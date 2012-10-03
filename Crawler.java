//
//  Crawler.java
//  
//
//  Created by doxuanhuy on 6/3/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

import java.text.*;
import java.awt.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.List;

public class Crawler {
	

	public static void main(String[] args){
		String strURL = args[0];
		URL url;
	
		Vector vectorSearched = new Vector();
		Vector vectorToSearch = new Vector();
		Vector linkList = new Vector();
		
		
		
		vectorSearched.removeAllElements();
		vectorToSearch.removeAllElements();
		linkList.removeAllElements();
		
		vectorToSearch.addElement(strURL);
		
		
		while(!vectorToSearch.isEmpty()){
			String currentURL;
			try{
				currentURL = vectorToSearch.elementAt(0).toString();
				url = new URL(currentURL);
				vectorSearched.addElement(strURL);
				vectorToSearch.remove(0);
			}catch(MalformedURLException e){
				return;
			}
			
			if(url.getProtocol().compareTo("http") != 0)
				return;
		
			try{
				URLConnection urlConnection = url.openConnection();
				urlConnection.setAllowUserInteraction(false);
			
				InputStream urlStream = url.openStream();
				String type = urlConnection.guessContentTypeFromStream(urlStream);
				System.out.print(type);
			
				//if(type == null) return;
				
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
			
				String lowerCaseContent = content.toLowerCase();
			
				int index = 0;
			
				while((index = lowerCaseContent.indexOf("<a", index)) != -1){
					if((index = lowerCaseContent.indexOf("href",index)) == -1)
						return;
					if((index = lowerCaseContent.indexOf("=",index)) == -1)
						return;
				
					index++;
				
					String remaining = content.substring(index);
				
					StringTokenizer st = new StringTokenizer(remaining,"\t\n\r\">#");
					String strLink = st.nextToken();
					
					if((!vectorToSearch.contains(strLink)) && (!vectorSearched.contains(strLink)))
						vectorToSearch.addElement(strLink);
					
					System.out.println(strLink);
				}
			}
			catch (IOException e) {
				return;
			}
		}
		
	}
}
