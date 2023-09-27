import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class WebCrawler implements Runnable{
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
                //reading the next line of content
                lineBuf = r.readLine();
            }
            catch(IOException e){
                //catching error when reading the next line
                System.out.println("Error parsing: " + e);
                return urls;
            }
            //checking to see if the line content is null(end of the page)
            if(lineBuf == null){
                return urls;
            }
            //adding a regular expression pattern to the line that was read
            Matcher m = urlPat.matcher(lineBuf);
            //adding to the URL linked list
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
            //the URL the webcrawler will be crawling
            url = new URL(this.url);
        }
        //catching the malformed URL of the created URL
        catch(MalformedURLException e){
            //printing out the bad URL
            System.out.println("Bad URL " + this.url + ": " + e);
            //ending the crawl
            crawler.done(this, this.url);
            return;
        }
        //reading the content of the URL
        BufferedReader r;
        try{
            //getting the content of the URL from the website
            r = Http.Get(url);
        }
        //catching network error
        catch(IOException e){
            //printing the error and the website
            System.out.println("IOException Http.Get " + this.url + ": " + e);
            //ending the crawl
            crawler.done(this, this.url);
            return;
        }
        //getting ready to crawl the next new URL
        for(String newUrl : this.parse(r)){
            crawler.addURL(newUrl);
        }
        //ending the crawl
        crawler.done(this, this.url);
    }
}

class VisitedURL{
    //the URL of the visited websites
    public String url;
    //the count(how many times we visted the website)
    public int visits;

    VisitedURL(String url){
        this.url = url;
    }
}

public class Crawler{
    //A list the holds the URL of the websites we want to crawl
    private List<String> queue = Collections.synchronizedList(new LinkedList<>());
    //A map that tracks the visited URl and the count of th website
    private Map<String, VisitedURL> visited = Collections.synchronizedMap(new LinkedHashMap<>());
    //maintaing the threads of the websites we are going to crawl
    private ArrayList<WebCrawler> threads = new ArrayList<>();
    //the max number of concurrent threads that we can run
    private int maxThreads;
    //initalize the max number of threads
    public Crawler(int maxThreads){
        this.maxThreads = maxThreads;
    }

    public void start(String entryPoint){
        //adding the starting point URL to start the crawl
        this.queue.add(entryPoint);
        //going to the queue and crawling to the next URL
        this.tryNext();
    }
    //stoping all crawling thread
    public synchronized void stop(){
        for(WebCrawler t : this.threads){
            //interrupt to stop the crawl
            t.t.interrupt();
        }
    }
    //checking to see if there are more URL's in the queue to crawl
    public synchronized boolean hasNext(){
        return this.queue.size() > 0;
    }

    public synchronized String next(){
        //getting the next URL in queue
        if(this.queue.size() == 0){
            return null;
        }  
        //removing the URL from the queue
        return this.queue.remove(0);
    }

    private void tryNext(){
        //checking to see if there is a new URL to crawl and checking the max thread limit
        if(!this.hasNext() || this.threads.size() == this.maxThreads){
            return;
        }

        String next = this.next();
        //if there is no new URL 
        if(next == null){
            //output that there is no new string
            System.out.println("Invalid next String");
            return;
        }
        //trying the next new URL(moving the queue up)
        this.threads.add(new WebCrawler(this, next));
    }

    public void done(WebCrawler t, String url){
        final VisitedURL obj = this.visited.putIfAbsent(url, new VisitedURL(url));
        //actively updating the visited websites
        if(obj == null){
            this.visited.get(url).visits++;
        }
        //remvoing from the queue
        this.threads.remove(t);
        //trying the next URL in the website
        this.tryNext();
    }

    public synchronized void addURL(String url){
        //checking to see if the URL is alreay in the queue
        if(this.queue.contains(url)){
            return;
        }
        //checking to see if we have already visited the URL
        if(this.visited.containsKey(url)){
            this.visited.get(url).visits++;
            return;
        }
        //adding the URL to the queue
        this.queue.add(url);
        //going to the next URL in the queue
        this.tryNext();
    }

    //returning the map of the visited URL's
    public Map<String, VisitedURL> getVisitedUrls(){
        return visited;
    }

}