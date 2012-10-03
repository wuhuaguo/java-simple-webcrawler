import java.text.*;
import java.awt.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.List;
import java.lang.Object;

public class test_connection{
	public static void main(String[] args){
		String strURL=args[0];
		URL url;
		try{
			url = new URL(strURL);
		}
		catch(MalformedURLException e){
			return;
		}
		
		try{
			URLConnection urlConnection = url.openConnection();
			urlConnection.setAllowUserInteraction(false);
		
			InputStream urlStream = url.openStream();
			String type = urlConnection.guessContentTypeFromStream(urlStream);
		
			byte b[] = new byte[1000];
			int numRead = urlStream.read(b);
			String content = new String(b,0, numRead);
			while(numRead != -1){
				numRead = urlStream.read(b);
				if(numRead != -1){
					String newContent = new String(b,0, numRead);
					content+= newContent;
				}
			}
			urlStream.close();
	
			System.out.println(content);
		
			String lowerCaseContent = content.toLowerCase();
			
			int index = lowerCaseContent.indexOf("<a");
			
			//System.out.println(index);
			
		}
		catch(Exception e){
			System.out.println("error!");
			return;
		}
	}
}