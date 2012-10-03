package pt.tumba.qbe;

import java.util.*;

/**
 * @author bmartins
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class StopURLs {

	/**
	 *  a list of stop sites for the related pages algorithm
	 */
	private static String[] stopURLList = {
		"www.sapo.pt",
		"tsf.sapo.pt",
		"exclusivos.sapo.pt",
		"www.clix.pt",
		"images.clix.pt",
		"pagewizard.clix.pt",
		"www.publico.pt",
		"ultimahora.publico.pt",
		"www.fccn.pt",
		"www.infocid.pt",
		"www.fct.mct.pt",
		"www.miau.pt",
		"www.sic.pt",
		"www.dn.pt",
		"www.negocios.pt",
		"www.diariodigital.pt",
		"www.expresso.pt",
		"www.abola.pt",
		"www.iol.pt"
	};

	/**
	 *  the hastable that stores the dictionary of stopwords
	 */
	private static Hashtable stopURLDictionary = new Hashtable();

	private static final StopURLs _theInstance = new StopURLs();

	public static StopURLs getInstance() {
		return _theInstance;
	}

	/**
	 */
	public static boolean isStopURL(String t) {
		if (t == null) {
			return false;
		}
		String term = t.toLowerCase();
		if (term.startsWith("http://")) {
			term = term.substring(7);
		}
		return stopURLDictionary.containsKey(term);
	}

	private StopURLs() {
		Integer int1 = new Integer(1);
		stopURLDictionary = new Hashtable();
		for (int i = stopURLList.length - 1; i >= 0; i--) {
			stopURLDictionary.put(stopURLList[i], int1);
		}
		stopURLList = null;
	}


}
