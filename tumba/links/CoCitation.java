package pt.tumba.links;

import java.util.*;

/**
 *  Bibliographic Co-Citation is a popular similarity measure used to establish a
 *  subject similarity between two items. If A and B are both cited by C, they 
 *  may be said to be related to one another, even though they don't directly
 *  reference each other. If A and B are both cited by many other items, they 
 *  have a stronger relationship. The more items they are cited by, the stronger
 *  their relationship is.
 * 
 *  Co-Citation was first proposed in the fields of citation analysis and bibliometrics as a
 *  fundamental metric to characterize the similarity between documents.
 *  There is a vast literature on citation analysis, sometimes called scientometrics,
 *  a term that was invented by V. V. Nalimov. The field blossomed with the advent
 *  of the Science Citation Index, which now covers over fifty years of source literature.
 *  The leading journals of the field are Scientometrics and the Journal of the American 
 *  Society of Information Science and Technology. 
 * 
 * @author Bruno Martins
 *
 */
public class CoCitation {

	/** The data structure containing the Web linkage graph */
	private WebGraph graph;

	/** A <code>Map</code> containing the CoCitation values for each page */
	private Map scores;
	
	/** 
	 * Constructor for CoCitation
	 * 
	 * @param graph The data structure containing the Web linkage graph
	 */
	public CoCitation ( WebGraph graph ) {
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
	 * Computes the CoCitation score for all the nodes with all the
         * other in the Web graph.
	 */
	public void computeCocitation() {
		for (int i=0; i<graph.numNodes(); i++) {
			computeCocitation(new Integer(i));
		}
	}
	
	/**
	 * Computes the cocitation score for a given link.
         *
	 * @param link The url for the link
	 */
	public void computeCocitation(String link) {
		computeCocitation(graph.URLToIdentifyer(link));
	}

	/**
	 * Returns the CoCitation score between a given link and all other links in the Web graph
	 * 
	 * @param link The url for the link
	 * @return A Map with the CoCitation score between the given link and all other
         *         links in the Web graph. Keys in the Map are link identifyers for all the other links,
         *         and values correspond to the CoCitation score
	 */
	public Map cocitation(String link) {
		return cocitation(graph.URLToIdentifyer(link));	
	}
	
	/**
	 * Returns the CoCitation score between two given links
	 * 
	 * @param link1 The url for one of the links
	 * @param link2 The url for the other link
	 * @return The CoCitation score between the given links
	 */
	public Double cocitation(String link1, String link2) {
		return cocitation(graph.URLToIdentifyer(link1),graph.URLToIdentifyer(link2));
	}
	
	/**
	 * Returns the CoCitation score between a given link identifyer and all other links in the Web graph
	 * 
	 * @param link The identifyer for the link
	 * @return A Map with the CoCitation score between the given link and all other
         *         links in the Web graph. Keys in the Map are link identifyers for all the other links,
         *         and values correspond to the CoCitation score
	 */
	private Map cocitation(Integer id) {
		if(id.intValue()!=0) {
			if (((Integer)(((Map)(scores.get(id))).get(new Integer(0)))).doubleValue()<0) computeCocitation(id);
		} else {
			if (((Integer)(((Map)(scores.get(new Integer(1)))).get(new Integer(0)))).doubleValue()<0) computeCocitation(id);
		}
		return (Map)(scores.get(id));
	}

	/**
	 * Returns the CoCitation score between two given link identifyers
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
         *
 	 * @param link1 The identifyer for one of the links
	 * @param link2 The identifyer for the other link
	 * @return The CoCitation score between the two given link identifyers
	 * @see WebGraph.IdentifyerToURL()
	 */
	private Double cocitation(Integer id1, Integer id2) {
		if(id1.equals(id2)) return new Double(1);
		if(id2.intValue()>id1.intValue()) {
			Integer id3 = id1; id1 = id2; id2 = id1; 
		}
		Double aux = (Double)(((Map)(scores.get(id1))).get(id2)); 
		if(aux.intValue()<0) return computeCocitation(id1,id2);
		return aux;
	}

	/**
	 * Computes the CoCitation score for a given link identifyer with all the
         * other links in the Web graph.
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
         *
 	 * @param link1 The identifyer for the link
	 * @see WebGraph.IdentifyerToURL()
	 */
	private void computeCocitation(Integer id) {
		for (int i=0; i<id.intValue(); i++) {
			computeCocitation(id,new Integer(i));
		}
		for (int i=id.intValue()+1; i<graph.numNodes(); i++) {
			computeCocitation(new Integer(i),id);
		}
	}

	/**
	 * Computes the CoCitation score between two given link identifyers.
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
         *
 	 * @param link1 The identifyer for one of the links
	 * @param link2 The identifyer for the other link
	 * @see WebGraph.IdentifyerToURL()
	 */
	private Double computeCocitation(Integer id1, Integer id2) {
		if(id1.equals(id2)) return new Double(1);
		if(id2.intValue()>id1.intValue()) {
			Integer id3 = id1; id1 = id2; id2 = id1; 
		}
		int score1 = 0;
		int score2 = 0;
		int score3 = 0;
		Double score;
		Map map1 = graph.inLinks(id1);
		Map map2 = graph.inLinks(id2);
		Iterator it = map1.keySet().iterator();
		while(it.hasNext()) {
			Integer aux = (Integer)(it.next());
			Double weight = (Double)(map2.get(aux));
			if(weight!=null && weight.doubleValue()>0) score1++;
			weight = (Double)(map1.get(aux));
			if(weight!=null && weight.doubleValue()>0) score2++;
		}
		it = map2.keySet().iterator();
		while(it.hasNext()) {
			Integer aux = (Integer)(it.next());
			Double weight = (Double)(map2.get(aux));
			if(weight!=null && weight.doubleValue()>0) score3++;
		}
		if((score2+score3)==0) score = new Double(0);
		else score = new Double(score1 / (score2 + score3));
		Map map3 = (Map)(scores.get(id1));
		map3.put(id2,score);
		scores.put(id1,map3);
		return score;
	}

}

