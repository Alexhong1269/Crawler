import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        //creating an instance of a crawler
        //makes 8 threads
        Crawler crawler = new Crawler(8);

        //starting the crawler at google.com
        System.out.println("Starting Crawler.");
        crawler.start("https://google.com");

        //The crawler is running
        Scanner scan = new Scanner(System.in);
        //crawler stops when the user types exit
        while(!scan.next().equals("exit"));
        //stoping the crawler
        crawler.stop();

        synchronized(crawler){
            //printing out the webistes that have been visited
            System.out.println("\n\n-----------------------------------");
            for(VisitedURL u : crawler.getVisitedUrls().values()){
                System.out.println(u.visits + "x " + u.url);
            }
            //printing out the visited URl
            //printing out the count(how many visits)
            System.out.println("\n\n-----------------------------------");
            System.out.println("Visited " + crawler.getVisitedUrls().size() + " unique urls");
        }
    }
}
