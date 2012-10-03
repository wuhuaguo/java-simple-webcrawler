package pt.tumba.links;

import java.util.*;

/**
 *  Amsler is a similarity measure used to establish a subject similarity between two items
 *  fusing both Bibliographic Coupling and Co-Citation. 
 *  Two items A and B are related if A and B are references by many same items,
 *  if A and B both reference many same items, or  A references a third item C that references B.
 * 
 *  The measure was proposed by Robert Amsler in "Applications of citation-based automatic
 *  classification", Linguistics Research Center, Univ. Texas at Austin, Technical Report 72-14, Dec. 1972.
 * 
 * @author Bruno Martins
 * @see CoCitation
 * @see Coupling
 */
public class Amsler {

	/** The data structure containing the Web linkage graph */
	private WebGraph graph;

	/** A <code>Map</code> containing the Amsler values for each page */
	private Map scores;
	
	/** 
	 * Constructor for Amsler
	 * 
	 * @param graph The data structure containing the Web linkage graph
	 */
	public Amsler ( WebGraph graph ) {
		this.graph = graph;
		this.scores = new HashMap();
		int numLinks = graph.numNodes();
		for(int i=0; i<numLinks; i++) { 
			HashMap aux = new HashMap();
			for(int j=0; j<i; j++) aux.put(new Integer(j),new Double(-1));	
			scores.put(new Integer(i),aux);
		}
	}
	
	/**
	 * Computes the Amsler score for all the nodes with all the
         * other in the Web graph.
	 */
	public void computeAmsler() {
		for (int i=0; i<graph.numNodes(); i++) {
			computeAmsler(new Integer(i));
		}
	}

	/**
	 * Computes the Amsler score for a given link.
         *
	 * @param link The url for the link
	 */
	public void computeAmsler(String link) {
		computeAmsler(graph.URLToIdentifyer(link));
	}
	
	/**
	 * Returns the Amsler score between a given link and all other links in the Web graph
	 * 
	 * @param link The url for the link
	 * @return A Map with the Amsler score between the given link and all other
         *         links in the Web graph. Keys in the Map are link identifyers for all the other links,
         *         and values correspond to the Amsler score
	 */
	public Map amsler(String link) {
		return amsler(graph.URLToIdentifyer(link));	
	}
	
	/**
	 * Returns the Amsler score between two given links
	 * 
	 * @param link1 The url for one of the links
	 * @param link2 The url for the other link
	 * @return The Amsler score between the two given links
	 */
	public Double amsler(String link1, String link2) {
		return amsler(graph.URLToIdentifyer(link1),graph.URLToIdentifyer(link2));
	}
	
	/**
	 * Returns the Amsler score between a given link identifyer and all other links in the Web graph
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
	 * 
	 * @param link The identifyer for the link
	 * @return A Map with the Amsler score between the given link and all other
         *         links in the Web graph. Keys in the Map are link identifyers for all the other links,
         *         and values correspond to the Amsler score
	 */
	private Map amsler(Integer id) {
		if(id.intValue()!=0) {
			if (((Integer)(((Map)(scores.get(id))).get(new Integer(0)))).doubleValue()<0) computeAmsler(id);
		} else {
			if (((Integer)(((Map)(scores.get(new Integer(1)))).get(new Integer(0)))).doubleValue()<0) computeAmsler(id);
		}
		return (Map)(scores.get(id));
	}

	/**
	 * Returns the Amsler score between two given link identifyers
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
         *
 	 * @param link1 The identifyer for one of the links
	 * @param link2 The identifyer for the other link
	 * @return The Amsler score between the two given link identifyers
	 * @see WebGraph.IdentifyerToURL()
	 */
	private Double amsler(Integer id1, Integer id2) {
		if(id1.equals(id2)) return new Double(1);
		if(id2.intValue()>id1.intValue()) {
			Integer id3 = id1; id1 = id2; id2 = id1; 
		}
		Double aux = (Double)(((Map)(scores.get(id1))).get(id2)); 
		if(aux.intValue()<0) return computeAmsler(id1,id2);
		return aux;
	}

	/**
	 * Computes the Amsler score for a given link identifyer with all the
         * other links in the Web graph.
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
         *
 	 * @param link1 The identifyer for the link
	 * @see WebGraph.IdentifyerToURL()
	 */
	private void computeAmsler(Integer id) {
		for (int i=0; i<id.intValue(); i++) {
			computeAmsler(id,new Integer(i));
		}
		for (int i=id.intValue()+1; i<graph.numNodes(); i++) {
			computeAmsler(new Integer(i),id);
		}
	}

	/**
	 * Computes the Amsler score between two given link identifyers.
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
         *
 	 * @param link1 The identifyer for one of the links
	 * @param link2 The identifyer for the other link
	 * @see WebGraph.IdentifyerToURL()
	 */
	private Double computeAmsler(Integer id1, Integer id2) {
		if(id1.equals(id2)) return new Double(1);
		if(id2.intValue()>id1.intValue()) {
			Integer id3 = id1; id1 = id2; id2 = id1; 
		}
		int score1 = 0;
		int score2 = 0;
		int score3 = 0;
		int score4 = 0;
		int score5 = 0;
		Double score;
		Map map1 = graph.inLinks(id1);
		Map map2 = graph.inLinks(id2);
		Map map3 = graph.outLinks(id1);
		Map map4 = graph.outLinks(id2);
		Iterator it = map1.keySet().iterator();
		while(it.hasNext()) {
			Integer aux = (Integer)(it.next());
			Double weight   = (Double)(map2.get(aux));
			Double weight2 = (Double)(map4.get(aux));
			if((weight!=null && weight.doubleValue()>0)||(weight2!=null && weight2.doubleValue()>0)) score1++;
			weight = (Double)(map1.get(aux));
			if(weight!=null && weight.doubleValue()>0) score2++;
		}
		it = map3.keySet().iterator();
		while(it.hasNext()) {
			Integer aux = (Integer)(it.next());
			Double weight   = (Double)(map2.get(aux));
			Double weight2 = (Double)(map4.get(aux));
			if((weight!=null && weight.doubleValue()>0)||(weight2!=null && weight2.doubleValue()>0)) score1++;
			weight = (Double)(map1.get(aux));
			if(weight!=null && weight.doubleValue()>0) score3++;
		}
		it = map2.keySet().iterator();
		while(it.hasNext()) {
			Integer aux = (Integer)(it.next());
			Double weight = (Double)(map2.get(aux));
			if(weight!=null && weight.doubleValue()>0) score4++;
		}
		it = map4.keySet().iterator();
		while(it.hasNext()) {
			Integer aux = (Integer)(it.next());
			Double weight = (Double)(map4.get(aux));
			if(weight!=null && weight.doubleValue()>0) score5++;
		}
		if((score2+score3+score4+score5)==0) score = new Double(0);
		else score = new Double(score1 / (score2 + score3 + score4 +score5));
		Map map5 = (Map)(scores.get(id1));
		map5.put(id2,score);
		scores.put(id1,map5);
		return score;
	}

}
