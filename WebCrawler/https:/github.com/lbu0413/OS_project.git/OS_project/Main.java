import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Crawler crawler = new Crawler(8);

        System.out.println("Starting Crawler.");
        crawler.start("https://google.com");

        Scanner scan = new Scanner(System.in);
        while(!scan.next().equals("exit"));

        crawler.stop();

        synchronized(crawler){
            System.out.println("\n\n-----------------------------------");
            for(VisitedURL u : crawler.getVisitedUrls().values()){
                System.out.println(u.visits + "x " + u.url);
            }

            System.out.println("\n\n-----------------------------------");
            System.out.println("Visited " + crawler.getVisitedUrls().size() + " unique urls");
        }
    }
}
