import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class will use the HTTP protocol to get a web page.
 * @author Stephen Li
 *
 */
public class URLGetter {
	
	private URL url;
	private HttpURLConnection connection;
	
	/**
	 * Creates a URL object from the given string.
	 * Opens the connection that will be used later.
	 * @param website the url to get the information from
	 */
	public URLGetter(String website) {
		
		try {
			url = new URL(website);
			
//			connection = new HttpURLConnection(url);
			URLConnection urlConnection = this.url.openConnection();
			connection = (HttpURLConnection) urlConnection;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This method will print the HTTP status codes for the connection.
	 */
	public void printStatusCode() {
		
		try {
			int code = connection.getResponseCode();
			String message = connection.getResponseMessage();
			
			System.out.println(code + " : " + message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This method will get the HTML contents of the page.
	 * @return the arraylist of lines of the webpage
	 */
	public ArrayList<String> getContents() {
		
		ArrayList<String> contents = new ArrayList<String>();
		
		try {
			Scanner in = new Scanner(connection.getInputStream());
			
			while (in.hasNextLine()) {
				String line = in.nextLine();
				contents.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		return contents;
		 
	}

}
