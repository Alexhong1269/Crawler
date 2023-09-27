import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {
    public static BufferedReader Get(URL url) throws IOException{
        //making a url connecntion
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        //going to perform a HTTP GET
        //to get data from that website
        con.setRequestMethod("GET");

        //identifying that the User-Agent is making the request
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");

        //checking for redirect and getting the URL if we encounter the status code for redirect
        con.setInstanceFollowRedirects(true);
        //getting the status code
        int statusCode = con.getResponseCode();

        boolean redirect = false;

        //checking for redirect and getting the URL if we encounter the status code for redirect
        if(statusCode != HttpURLConnection.HTTP_OK){
            if(statusCode == HttpURLConnection.HTTP_MOVED_TEMP || statusCode == HttpURLConnection.HTTP_MOVED_PERM || statusCode == HttpURLConnection.HTTP_SEE_OTHER){
                redirect = true;
            }
        }

        if(redirect){
            String newUrl = con.getHeaderField("Location");

            String cookies = con.getHeaderField("Set-Cookie");

            return Http.Get(new URL(newUrl));
        }
        //if there is no redirect we return the URL
        return new BufferedReader(new InputStreamReader(con.getInputStream()));
    }
}
