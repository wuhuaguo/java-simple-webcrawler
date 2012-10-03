package pt.tumba.links;

import java.util.*;

/**
 *  Bibliographic Coupling is a popular similarity measure used to establish a
 *  subject similarity between two items. It operates on a similar principle to that
 *  of Co-Citation, but in a way it is its mirror image.  Bibliographic coupling 
 *  links two items that reference the same items, so that if A and B both reference C,
 *  they may be said to be related, even though they don't directly reference each other.
 *  The more items they both reference in common, the stronger their relationship is.
 * 
 *  Bibliographic Coupling was first proposed in the fields of citation analysis and bibliometrics as a
 *  fundamental metric to characterize the similarity between documents.
 *  There is a vast literature on citation analysis, sometimes called scientometrics,
 *  a term that was invented by V. V. Nalimov. The field blossomed with the advent
 *  of the Science Citation Index, which now covers over fifty years of source literature.
 *  The leading journals of the field are Scientometrics and the Journal of the American 
 *  Society of Information Science and Technology. 
 * 
 * @author Bruno Martins
 * @see CoCitation
 *
 */
public class Coupling {

	/** The data structure containing the Web linkage graph */
	private WebGraph graph;

	/** A <code>Map</code> containing the Coupling values for each page */
	private Map scores;
	
	/** 
	 * Constructor for Coupling
	 * 
	 * @param graph The data structure containing the Web linkage graph
	 */
	public Coupling ( WebGraph graph ) {
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
	 * Computes the Coupling score for all the nodes with all the
         * other in the Web graph.
	 */
	public void computeCoupling() {
		for (int i=0; i<graph.numNodes(); i++) {
			computeCoupling(new Integer(i));
		}
	}
	
	/**
	 * Computes the Coupling score for a given link.
         *
	 * @param link The url for the link
	 */
	public void computeCoupling(String link) {
		computeCoupling(graph.URLToIdentifyer(link));
	}

	/**
	 * Returns the Coupling score between a given link and all other links in the Web graph
	 * 
	 * @param link The url for the link
	 * @return A Map with the Coupling score between the given link and all other
         *         links in the Web graph. Keys in the Map are link identifyers for all the other links,
         *         and values correspond to the Coupling score
	 */
	public Map coupling(String link) {
		return coupling(graph.URLToIdentifyer(link));	
	}
	
	/**
	 * Returns the Coupling score between two given links
	 * 
	 * @param link1 The url for one of the links
	 * @param link2 The url for the other link
	 * @return The Coupling score between the given links
	 */
	public Double coupling(String link1, String link2) {
		return coupling(graph.URLToIdentifyer(link1),graph.URLToIdentifyer(link2));
	}
	
	/**
	 * Returns the Coupling score between a given link identifyer and all other links in the Web graph
	 * 
	 * @param link The identifyer for the link
	 * @return A Map with the Coupling score between the given link and all other
         *         links in the Web graph. Keys in the Map are link identifyers for all the other links,
         *         and values correspond to the Coupling score
	 */
	private Map coupling(Integer id) {
		if(id.intValue()!=0) {
			if (((Integer)(((Map)(scores.get(id))).get(new Integer(0)))).doubleValue()<0) computeCoupling(id);
		} else {
			if (((Integer)(((Map)(scores.get(new Integer(1)))).get(new Integer(0)))).doubleValue()<0) computeCoupling(id);
		}
		return (Map)(scores.get(id));
	}

	/**
	 * Returns the Coupling score between two given link identifyers
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
         *
 	 * @param link1 The identifyer for one of the links
	 * @param link2 The identifyer for the other link
	 * @return The Coupling score between the two given link identifyers
	 * @see WebGraph.IdentifyerToURL()
	 */
	private Double coupling(Integer id1, Integer id2) {
		if(id1.equals(id2)) return new Double(Integer.MAX_VALUE);
		if(id2.intValue()>id1.intValue()) {
			Integer id3 = id1; id1 = id2; id2 = id1; 
		}
		Double aux = (Double)(((Map)(scores.get(id1))).get(id2)); 
		if(aux.intValue()<0) return computeCoupling(id1,id2);
		return aux;
	}

	/**
	 * Computes the Coupling score for a given link identifyer with all the
         * other links in the Web graph.
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
         *
 	 * @param link1 The identifyer for the link
	 * @see WebGraph.IdentifyerToURL()
	 */
	private void computeCoupling(Integer id) {
		for (int i=0; i<id.intValue(); i++) {
			computeCoupling(id,new Integer(i));
		}
		for (int i=id.intValue()+1; i<graph.numNodes(); i++) {
			computeCoupling(new Integer(i),id);
		}
	}

	/**
	 * Computes the Coupling score between two given link identifyers.
	 * Identifyers are Integer numberes, used in <code>WebGraph</code> to
	 * represent the Web graph for efficiency reasons.
         *
 	 * @param link1 The identifyer for one of the links
	 * @param link2 The identifyer for the other link
	 * @see WebGraph.IdentifyerToURL()
	 */
	private Double computeCoupling(Integer id1, Integer id2) {
		if(id1.equals(id2)) return new Double(1);
		if(id2.intValue()>id1.intValue()) {
			Integer id3 = id1; id1 = id2; id2 = id1; 
		}
		int score1 = 0;
		int score2 = 0;
		int score3 = 0;
		Double score;
		Map map1 = graph.outLinks(id1);
		Map map2 = graph.outLinks(id2);
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
