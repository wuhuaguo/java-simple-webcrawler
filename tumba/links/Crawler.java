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
import java.lang.Object;

public class Crawler {
	
	public static final int Maximum = 100;
	
	public String URLParser(String host,String path){
		
		return path;
	}
	

/////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////
	public static void main(String[] args){
		String strURL = args[0];
		String host;
		host = strURL;
		URL url;
		FileOutputStream fout;
		int capacity;
		int loop;
		int loopTime = Maximum;
/////////////////////////////////////////////////////////////////////
/*Graph construct*/
		try{
			fout = new FileOutputStream("file.txt");
		}
		catch (IOException e) {
			return;
		}
		
		
		
		Vector vectorSearched = new Vector();
		Vector vectorToSearch = new Vector();
		Vector linkList = new Vector();
		
		
		vectorSearched.removeAllElements();
		vectorToSearch.removeAllElements();
		linkList.removeAllElements();
		
		vectorToSearch.addElement(strURL);
		
/*loop checking whether a link is search or not, needed to search link exists, open and crawl*/		
		while(!vectorToSearch.isEmpty() && loopTime>=0){
			
			String currentURL;
/*open a link at top of toSearch[Vector], add to searched[Vector]*/			
			try{
				currentURL = vectorToSearch.elementAt(0).toString();
				
				url = new URL(currentURL);
				vectorSearched.addElement(strURL);	
				vectorToSearch.remove(0);
			}catch(MalformedURLException e){
				System.out.println("exception: "+ e.getMessage());
				e.printStackTrace();
				continue;
			}
			System.out.println("is Crawling: "+currentURL);
			
			if(url.getProtocol().compareTo("http") != 0)
				continue;
		
			try{
				URLConnection urlConnection = url.openConnection();
				urlConnection.setAllowUserInteraction(false);
				
				
				/////////////////////////////
				loopTime--;
				System.out.println("--looptime: "+loopTime);
				
				
				InputStream urlStream = url.openStream();
				String type = urlConnection.guessContentTypeFromStream(urlStream);
				//debug--System.out.print(type);
			
				//debug--if(type == null) return;
				
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
				
				//System.out.println(content);
				
				String lowerCaseContent = content.toLowerCase();
			
				int index = 0;
				
				if((index= lowerCaseContent.indexOf("<a href")) == -1) continue;
				else{
					System.out.println(" index: "+index);
				}
				while((index = lowerCaseContent.indexOf("<a", index)) != -1){
					
					
					//System.out.println("                           ..............working");
					if((index = lowerCaseContent.indexOf("href",index)) == -1)
						continue;
					if((index = lowerCaseContent.indexOf("=",index)) == -1)
						continue;
						
						
				
					index++;
				
					String remaining = content.substring(index);
				
					StringTokenizer st = new StringTokenizer(remaining,"\t\n\r\">#");
					String strLink = st.nextToken();
					
					if(!strLink.startsWith("http") && !strLink.startsWith("ftp") ) 
						strLink = host + strLink;
					
					
					/*
					Integer id1 = graph.URLToIdentifyer(currentURL);
					Integer id2 = graph.URLToIdentifyer(strLink);
					
					System.out.println(id1+" "+id2);
					
					System.out.println(currentURL+" "+strLink);
					*/
					
										
					if((!vectorToSearch.contains(strLink)) && (!vectorSearched.contains(strLink)))
						{vectorToSearch.addElement(strLink);
						 System.out.println("    "+strLink+"is added");
						}
					///////////////////////////add link bw currentURL to URLs inside its					
						if(!linkList.contains(strLink)) linkList.addElement(strLink);
						{	
							new PrintStream(fout).println(currentURL+"->"+strLink+" 1.0");
							//debug-System.out.println(strLink);
						}
				
				//////test:Write to file, and read from file to webGraph datastructure
					
					
					 
				}
				
				
			
			
/*compute hub and authority*/			
			/*HITS hits = new HITS(graph);
			
			for(loop=0; loop<= linkList.size(); loop++){
				System.out.println(hits.authorityScore(graph.IdentifyerToURL(loop))+" "+hits.hubScore(graph.IdentifyerToURL(loop)));
			 }
			 */
/////////////////////////////////////////////////////////////////////////		
/////////////////////////////////////////////////////////////////////////
		
		File file = new File("file.txt");
		WebGraph graph = new WebGraph(file);
				
				
		Map hubScores= new HashMap();
		Map authorityScores = new HashMap();
		
		
		int numLinks = graph.numNodes();
		for(int i=1; i<=numLinks; i++){
			hubScores.put(new Integer(i), new Double(1));
			authorityScores.put(new Integer(i), new Double(1));
		}
		
		
		//HITS hits_test = new HITS(graph);
		
		boolean change = true;
		int numIterator=1 ;
		
		while (numIterator-->0 && change) {
			change = false;
			for(int i=1; i<=graph.numNodes(); i++){
				//debug- System.out.println("current i "+i);
				Map inlinks = graph.inLinks(i);
				Map outlinks = graph.outLinks(i);
				Iterator inIter = inlinks.keySet().iterator();
				Iterator outIter = outlinks.keySet().iterator();
				
				double authorityScore =0;
				double hubScore =0;
				
				Integer m,n;
				
				///////////////////////////////////////
				
				while(inIter.hasNext()){
					m = (Integer)inIter.next();
					//System.out.println("inIter:inlinks key "+m);
					//System.out.println("hubScores "+hubScores.get(m));
					authorityScore += (Double)hubScores.get(m);
				}
				while(outIter.hasNext()){
					n= (Integer)outIter.next();
					//System.out.println("outIter:outlinks key "+n);
					//System.out.println("authorityScores "+authorityScores.get(n));
					hubScore += (Double)authorityScores.get(n);
				}
				
				//////////////////////////////////////////
				
				Double authorityScore2 =(Double)(authorityScores.get(new Integer(i)));
				Double hubScore2 = (Double)(hubScores.get(new Integer(i)));
				if(authorityScore2.doubleValue()!= authorityScore){
					change = true;
					authorityScores.put(new Integer(i),new Double(authorityScore));
				}
				if(hubScore2.doubleValue()!= hubScore){
					change = true;
					hubScores.put(new Integer(i), new Double(hubScore));
				}				
				
				//////////////////////////////////////////
				
			}
		}
		
		Iterator hubIter= hubScores.keySet().iterator();
		Iterator authorIter = authorityScores.keySet().iterator();


		//////////result print/////////////////////
		
		
		
		/*
		while (hubIter.hasNext()) {
			Integer temp1= (Integer)hubIter.next();
			System.out.println("url "+temp1+" "+graph.IdentifyerToURL(temp1));
			System.out.println("hubScore "+temp1+" "+hubScores.get(temp1));
		}
		
		while (authorIter.hasNext()) {
			Integer temp2= (Integer)authorIter.next();
			System.out.println("url"+temp2+" "+graph.IdentifyerToURL(temp2));
			System.out.println("AuthorityScore "+temp2+" "+authorityScores.get(temp2));
		}
		*/
		}
	
			catch (IOException e) {
				System.out.println("error!");
				continue;
			}
		}

	}
}



