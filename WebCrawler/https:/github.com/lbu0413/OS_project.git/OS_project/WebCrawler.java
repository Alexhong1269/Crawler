import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class WebCrawler implements Runnable{
    //
    final static Pattern urlPat = Pattern.compile("https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    //variables needed to crawl thread
    Crawler crawler;
    //The url
    String url;
    //the thread the bot to crawl on
    public Thread thread;

    //function for the bot to crawl on the thread
    public WebCrawler(Crawler crawler, String url){
        this.crawler = crawler;
        this.url = url;
        this.thread = new Thread(this, "CrawlThread");
        //The start of the thread
        thread.start();
    }

    private LinkedList<String> parse(BufferedReader r){
        //base case to seperate the links
        String lineBuf = " ";

        //linked list to hold the urls
        LinkedList<String> urls = new LinkedList<String>();

        do{
            try{
                //
                lineBuf = r.readLine();
            }
            catch(IOException e){
                System.out.println("Error parsing: " + e);
                return urls;
            }
            if(lineBuf == null){
                return urls;
            }

            Matcher m = urlPat.matcher(lineBuf);
            while(m.find()){
                urls.add(m.group(0));
            }
        }
        while(lineBuf != null);
        return urls;
    }

    public void run(){
        URL url;
        try{
            url = new URL(this.url);
        }
        catch(MalformedURLException e){
            System.out.println("Bad URL " + this.url + ": " + e);
            crawler.done(this, this.url);
            return;
        }
        BufferedReader r;
        try{
            r = Http.Get(url);
        }
        catch(IOException e){
            System.out.println("IOException Http.Get " + this.url + ": " + e);
            crawler.done(this, this.url);
            return;
        }
        for(String newUrl : this.parse(r)){
            crawler.addURL(newUrl);
        }
        crawler.done(this, this.url);
    }
}

class VisitedURL{
    public String url;
    public int visits;

    VisitedURL(String url){
        this.url = url;
    }
}

public class Crawler{
    private List<String> queue = Collections.synchronizedList(new LinkedList<>());

    private Map<String, VisitedURL> visited = Collections.synchronizedMap(new LinkedHashMap<>());

    private ArrayList<WebCrawler> threads = new ArrayList<>();

    private int maxThreads;

    public Crawler(int maxThreads){
        this.maxThreads = maxThreads;
    }

    public void start(String entryPoint){
        this.queue.add(entryPoint);
        this.tryNext();
    }

    public synchronized void stop(){
        for(WebCrawler t : this.threads){
            
            t.t.interrupt();
        }
    }

    public synchronized boolean hasNext(){
        return this.queue.size() > 0;
    }

    public synchronized String next(){
        if(this.queue.size() == 0){
            return null;
        }
        return this.queue.remove(0);
    }

    private void tryNext(){
        if(!this.hasNext() || this.threads.size() == this.maxThreads){
            return;
        }

        String next = this.next();
        if(next == null){
            System.out.println("Invalid next String");
            return;
        }
        this.threads.add(new WebCrawler(this, next));
    }

    public void done(WebCrawler t, String url){
        final VisitedURL obj = this.visited.putIfAbsent(url, new VisitedURL(url));

        if(obj == null){
            this.visited.get(url).visits++;
        }

        this.threads.remove(t);
        this.tryNext();
    }

    public synchronized void addURL(String url){
        if(this.queue.contains(url)){
            return;
        }
        if(this.visited.containsKey(url)){
            this.visited.get(url).visits++;
            return;
        }
        this.queue.add(url);

        this.tryNext();
    }

    public Map<String, VisitedURL> getVisitedUrls(){
        return visited;
    }

}