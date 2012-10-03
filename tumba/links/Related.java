package pt.tumba.qbe;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 *  Description of the Class
 *
 *@author     Bruno Martins
 *@created    26 de Fevereiro de 2003
 */
public class Related {
    private static boolean debug = true;
	
	private int maxLinks;
	
	private Logger logger;
	private static int maxLevels = 2;
	
	private Connection conn = null; 

	private Map cocitations;
	private Map couplings;
	private Map siblings;
	
	private Map id_to_url;
	private Map url_to_id;
	
	private Map url_authority_score;
	private Map url_hub_score;
	
	private Map url_from_urls;
	private Map url_to_urls;

	private Random rand;

	private long url_id = 0;
	
    /**
     *  Description of the Method
     *
     *@param  args  Description of the Parameter
     */
    public static void main(String args[]) {
        String page = args[0];
        page = "http://cosmo.fis.fc.ul.pt/~paguiar";
        Related s = new Related((Connection)null,null,500);
        long time1 = System.currentTimeMillis();
        List v = s.getRelated(page);
        time1 = System.currentTimeMillis() - time1;
        for (int i = 0; i < v.size(); i++) {
            System.out.println(i + " " + (Long) (v.get(i)));
        }
        System.out.println(" --- " + page + " --- " + s.url_to_id.size() + " --- " + v.size() + " --- " + time1 + " millies");
    }

	/**
	 *  Constructor for the Related object
	 *
	 *@param  arquive  Description of the Parameter
	 */
	public Related(Connection conn, Logger logger, int maxLinks) {
		this.maxLinks = maxLinks;
		if(logger!=null) this.logger = logger; 
		else this.logger = Logger.getLogger(Related.class);
		url_id = 0;
		url_to_id = new HashMap();
		id_to_url = new HashMap();
		url_hub_score = new HashMap();
		url_authority_score = new HashMap();
		url_to_urls = new HashMap();
		url_from_urls = new HashMap();
		cocitations = new HashMap();
		couplings = new HashMap();
		siblings = new HashMap();
		rand = new Random(0);
		this.conn = conn;
	}


    /**
     *  Adds a feature to the URL attribute of the Related object
     *
     *@param  url     The feature to be added to the URL attribute
     *@param  cocite  The feature to be added to the URL attribute
     *@return         Description of the Return Value
     */
    Long addURL(Long url, boolean cocite) {
        if (url.longValue() == 0) {
            return url;
        }
        Long id;
        if ((id = (Long) (url_to_id.get(url))) == null) {
            id = new Long(url_id++);
            url_to_id.put(url, id);
            id_to_url.put(id, url);
            url_hub_score.put(id, new Double(1));
            url_authority_score.put(id, new Double(1));
            url_to_urls.put(id, new HashMap());
            url_from_urls.put(id, new HashMap());
            if (cocite) {
                cocitations.put(id, null);
                couplings.put(id, null);
            }
        }
        Double sibValue = (Double) (siblings.get(id));
        if (sibValue == null) {
            sibValue = new Double(1);
        } else {
            sibValue = new Double(sibValue.doubleValue() + 1);
        }
        siblings.put(id, sibValue);
        return id;
    }


    /**
     *  Adds a feature to the URL attribute of the Related object
     *
     *@param  fromURL  The feature to be added to the URL attribute
     *@param  toURL    The feature to be added to the URL attribute
     */
    public void addURL(Long fromURL, Iterator toURL) {
        Long fromID = addURL(fromURL, false);
        HashMap to_urls = (HashMap) (url_to_urls.get(fromID));
        Object trash = null;
        while (toURL.hasNext()) {
            Long aux = (Long) (toURL.next());
            Long toID = addURL(aux, false);
            HashMap from_urls = (HashMap) (url_from_urls.get(toID));
            to_urls.put(toID, trash);
            from_urls.put(fromID, trash);
            url_from_urls.put(toID, from_urls);
        }
        url_to_urls.put(fromID, to_urls);
    }


    /**
     *  Adds a feature to the URL attribute of the Related object
     *
     *@param  fromURL  The feature to be added to the URL attribute
     *@param  toURL    The feature to be added to the URL attribute
     */
    public synchronized void addURL(Long fromURL, Long toURL) {
        if (fromURL.longValue() == 0 || toURL.longValue() == 0) {
            return;
        }
        Object trash = null;
        Long fromID = addURL(fromURL, false);
        Long toID = addURL(toURL, false);
        HashMap to_urls = (HashMap) (url_to_urls.get(fromID));
        HashMap from_urls = (HashMap) (url_from_urls.get(toID));
        to_urls.put(toID, trash);
        from_urls.put(fromID, trash);
        url_to_urls.put(fromID, to_urls);
        url_from_urls.put(toID, from_urls);
    }


    /**
     *  Description of the Method
     *
     *@param  url         Description of the Parameter
     *@param  return_url  Description of the Parameter
     *@return             Description of the Return Value
     */
    List CoCitation(Long url, boolean return_url) {
        Iterator it;
        Long id = (Long) (url_to_id.get(url));
        HashMap urls = (HashMap) (cocitations.get(id));
        if (urls == null) {
            return new Vector();
        }
        Vector v = new Vector();
        it = urls.keySet().iterator();
        while (it.hasNext()) {
            v.addElement((Long) (it.next()));
        }
        Stack stack = new Stack();
        int low = 0;
        int high = v.size() - 1;
        int p;
        int i;
        int pivotIndex;
        int value;
        double ind1;
        double ind2;
        double ind3;
        double ind4;
        stack.push(new Integer(high));
        stack.push(new Integer(low));
        if (high <= 0) {
            return v;
        }
        while (!stack.empty()) {
            low = ((Integer) stack.pop()).intValue();
            high = ((Integer) stack.pop()).intValue();
            value = rand.nextInt();
            pivotIndex = low + (((value < 0) ? -1 * value : value) % (high + 1 - low));
            exchangePos(v, low, pivotIndex);
            for (p = low, i = low + 1; i <= high; i += 1) {
                ind1 = -((Double) (urls.get(v.elementAt(i)))).doubleValue();
                ind2 = -((Double) (urls.get(v.elementAt(low)))).doubleValue();
                ind3 = -getAuthorityScoreFromID((Long) (v.elementAt(i)));
                ind4 = -getAuthorityScoreFromID((Long) (v.elementAt(low)));
                if (ind1 < ind2 || (ind1 == ind2 && ind3 < ind4)) {
                    exchangePos(v, ++p, i);
                }
            }
            exchangePos(v, low, p);
            if (low < (p - 1)) {
                stack.push(new Integer(p - 1));
                stack.push(new Integer(low));
            }
            if ((p + 1) < high) {
                stack.push(new Integer(high));
                stack.push(new Integer(p + 1));
            }
        }
        if (return_url) {
            for (i = 0; i < v.size(); ) {
                if (((Double) (urls.get(v.elementAt(i)))).doubleValue() > 0.0) {
                    v.setElementAt(id_to_url.get(v.elementAt(i)), i++);
                } else {
                    v.removeElementAt(i);
                }
            }
        }
        return v;
    }


    /**
     *  Description of the Method
     */
    public void compute() {
        computeHITS();
        computeCocitations();
        computeCouplings();
        computeSiblings();
    }


    /**
     *  Description of the Method
     */
    public void computeCocitations() {
        Iterator it2 = cocitations.keySet().iterator();
        Iterator it;
        while (it2.hasNext()) {
            Long id = (Long) (it2.next());
            HashMap aux;
            HashMap urls = new HashMap();
            it = id_to_url.keySet().iterator();
            while (it.hasNext()) {
                urls.put(it.next(), new Double(0));
            }

            if ((aux = (HashMap) (url_to_urls.get(id))) != null) {
                long maxValue = 0;
                Iterator parent_urls = aux.keySet().iterator();
                while (parent_urls.hasNext()) {
                    Long id2 = (Long) (parent_urls.next());
                    if ((aux = (HashMap) (url_from_urls.get(id2))) != null) {
                        Iterator child_urls = aux.keySet().iterator();
                        while (child_urls.hasNext()) {
                            Long id3 = (Long) (child_urls.next());

                            long intv = ((Double) (urls.get(id3))).longValue() + 1;
                            if (intv > maxValue) {
                                maxValue = intv;
                            }
                            urls.put(id3, new Double(intv));

                            /*
                             *  HashMap aux2 = (HashMap) (url_to_urls.get(id3));
                             *  Iterator ita = aux2.keySet().iterator();
                             *  while(ita.hasNext()) {
                             *  Long id4 = (Long) (ita.next());
                             *  if (aux.containsKey(id4)) {
                             *  int intv = ((Double) (urls.get(id3))).longValue() + 1;
                             *  if (intv > maxValue) maxValue = intv;
                             *  urls.put(id3, new Double(intv));
                             *  }
                             *  }
                             */
                        }
                    }
                }
                it = urls.keySet().iterator();
                if (maxValue > 0) {
                    while (it.hasNext()) {
                        Object auxobj = it.next();
                        urls.put(auxobj, new Double(((Double) (urls.get(auxobj))).doubleValue() / maxValue));
                    }
                }
                cocitations.put(id, urls);
            }
        }
    }


    /**
     *  Description of the Method
     */
    public void computeCouplings() {
        Iterator it2 = couplings.keySet().iterator();
        Iterator it;
        while (it2.hasNext()) {
            long maxValue = 0;
            Long id = (Long) (it2.next());
            HashMap aux;
            HashMap aux2;
            HashMap urls = new HashMap();
            it = id_to_url.keySet().iterator();
            while (it.hasNext()) {
                urls.put(it.next(), new Double(0));
            }

            if ((aux = (HashMap) (url_from_urls.get(id))) != null) {
                Iterator it1 = aux.keySet().iterator();
                while (it1.hasNext()) {
                    Long id2 = (Long) (it1.next());
                    if ((aux2 = (HashMap) (url_to_urls.get(id2))) != null) {
                        Iterator it3 = aux2.keySet().iterator();
                        while (it3.hasNext()) {
                            Long id3 = (Long) (it3.next());
                            long intv = ((Double) (urls.get(id3))).longValue() + 1;
                            if (intv > maxValue) {
                                maxValue = intv;
                            }
                            urls.put(id3, new Double(intv));
                        }
                    }
                }
            }
            it = urls.keySet().iterator();
            if (maxValue > 0) {
                while (it.hasNext()) {
                    Object auxobj = it.next();
                    urls.put(auxobj, new Double(((Double) (urls.get(auxobj))).doubleValue() / maxValue));
                }
            }
            couplings.put(id, urls);
        }
    }


    /**
     *  Description of the Method
     */
    public void computeHITS() {
        int numIterations = 25;
        boolean change = true;
        for (int i = 0; i < numIterations && change; i++) {
            change = false;
            double max_hub = 0;
            double max_auth = 0;
            for (int j = 0; j < url_id; j++) {
                double hub_score = 0;
                double autority_score = 0;
                Long id = new Long(j);
                Iterator to_urls = ((HashMap) (url_to_urls.get(id))).keySet().iterator();
                Iterator from_urls = ((HashMap) (url_from_urls.get(id))).keySet().iterator();
                while (from_urls.hasNext()) {
                    autority_score += getHubScoreFromID((Long) (from_urls.next()));
                }
                Double new_auth_score = new Double(autority_score);
                if (autority_score > max_auth) {
                    max_auth = autority_score;
                }
                if (!url_authority_score.get(id).equals(new_auth_score)) {
                    change = true;
                    url_authority_score.put(id, new_auth_score);
                }
                while (to_urls.hasNext()) {
                    hub_score += getAuthorityScoreFromID((Long) (to_urls.next()));
                }
                if (hub_score > max_hub) {
                    max_hub = hub_score;
                }
                Double new_hub_score = new Double(hub_score);
                if (!url_hub_score.get(id).equals(new_hub_score)) {
                    change = true;
                    url_hub_score.put(id, new_hub_score);
                }
            }
            if (change) {
                for (int j = 0; j < url_id; j++) {
                    Long id = new Long(j);
                    if (max_hub > 0) {
                        url_hub_score.put(id, new Double(((Double) (url_hub_score.get(id))).doubleValue() / max_hub));
                    }
                    if (max_auth > 0) {
                        url_authority_score.put(id, new Double(((Double) (url_authority_score.get(id))).doubleValue() / max_auth));
                    }
                }
            }
        }
    }


    /**
     *  Description of the Method
     */
    public void computeSiblings() {
        double min = url_id + 1;
        double max = 0;
        Iterator it2 = siblings.keySet().iterator();
        while (it2.hasNext()) {
            Long id = (Long) (it2.next());
            double value = ((Double) (siblings.get(id))).doubleValue();
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }
        it2 = siblings.keySet().iterator();
        while (it2.hasNext()) {
            Long id = (Long) (it2.next());
            double value = (((Double) (siblings.get(id))).doubleValue() - min) / max;
            siblings.put(id, new Double(value));
        }
    }


    /**
     *  Description of the Method
     *
     *@param  url         Description of the Parameter
     *@param  return_url  Description of the Parameter
     *@return             Description of the Return Value
     */
    List Coupling(Long url, boolean return_url) {
        Long id = (Long) (url_to_id.get(url));
        HashMap urls = (HashMap) (couplings.get(id));
        Iterator it;
        if (urls == null) {
            return new Vector();
        }
        Vector v = new Vector();
        it = urls.keySet().iterator();
        while (it.hasNext()) {
            v.addElement((Long) (it.next()));
        }
        Stack stack = new Stack();
        int low = 0;
        int high = v.size() - 1;
        int p;
        int i;
        int pivotIndex;
        int value;
        double ind1;
        double ind2;
        double ind3;
        double ind4;
        stack.push(new Integer(high));
        stack.push(new Integer(low));
        if (high <= 0) {
            return v;
        }
        while (!stack.empty()) {
            low = ((Integer) stack.pop()).intValue();
            high = ((Integer) stack.pop()).intValue();
            value = rand.nextInt();
            pivotIndex = low + (((value < 0) ? -1 * value : value) % (high + 1 - low));
            exchangePos(v, low, pivotIndex);
            for (p = low, i = low + 1; i <= high; i += 1) {
                ind1 = -((Double) (urls.get(v.elementAt(i)))).doubleValue();
                ind2 = -((Double) (urls.get(v.elementAt(low)))).doubleValue();
                ind3 = -getAuthorityScoreFromID((Long) (v.elementAt(i)));
                ind4 = -getAuthorityScoreFromID((Long) (v.elementAt(low)));
                if (ind1 < ind2 || (ind1 == ind2 && ind3 < ind4)) {
                    exchangePos(v, ++p, i);
                }
            }
            exchangePos(v, low, p);
            if (low < (p - 1)) {
                stack.push(new Integer(p - 1));
                stack.push(new Integer(low));
            }
            if ((p + 1) < high) {
                stack.push(new Integer(high));
                stack.push(new Integer(p + 1));
            }
        }
        if (return_url) {
            for (i = 0; i < v.size(); ) {
                if (((Double) (urls.get(v.elementAt(i)))).doubleValue() > 0.0) {
                    v.setElementAt(id_to_url.get(v.elementAt(i)), i++);
                } else {
                    v.removeElementAt(i);
                }
            }
        }
        return v;
    }


    /**
     *  Description of the Method
     *
     *@param  outbound  Description of the Parameter
     *@param  layer     Description of the Parameter
     *@param  levels    Description of the Parameter
     *@param  id        Description of the Parameter
     *@return           Description of the Return Value
     */
    private String escreveQuery(boolean outbound, int levels, long id) {
        String queries[] = new String[levels];
        String queries2[] = new String[levels];
       	String column1 = " pai ";
		String column2 = " filho ";
		if (outbound) {
			column1 = " filho ";
			column2 = " pai ";
		}
		queries[0] = "select" + column1 + "," + column2 + " from ligacoes_pesos where" + column2 + "=" + id + " or" + column1 + "=" + id;
		queries2[0] = "select" + column1 + " from ligacoes_pesos where" + column2 + "=" + id + " union select" + column2 + " from ligacoes_pesos where" + column1 + "=" + id;
		for (int i = 1; i < levels; i++) {
			queries[i] = "select" + column1 + "," + column2 + " from ligacoes_pesos where" + column2 + "in (" + queries2[i - 1] + ") or" + column1 + "in (" + queries2[i - 1] + ")";
			queries2[i] = "select" + column1 + " from ligacoes_pesos where" + column2 + "in (" + queries2[i - 1] + ") union select" + column2 + " from ligacoes_pesos where" + column1 + "in (" + queries2[i - 1] + ")";
			queries[0] = queries[0] + " union " + queries[i];
		}
        return queries[0] + " and rownum <= " + maxLinks;
    }


    /**
     *  Description of the Method
     *
     *@param  v   Description of the Parameter
     *@param  p1  Description of the Parameter
     *@param  p2  Description of the Parameter
     *@return     Description of the Return Value
     */
    List exchangePos(List v, int p1, int p2) {
        Object tmp = v.get(p1);
        v.set(p1,v.get(p2));
        v.set(p2,tmp);
        return v;
    }


    /**
     *  Gets the authorityScore attribute of the Related object
     *
     *@param  url  Description of the Parameter
     *@return      The authorityScore value
     */
    public double getAuthorityScore(String url) {
        Long id = (Long) (url_to_id.get(new Long(((Object) (url.toString())).hashCode())));
        Double aux = (Double) (url_authority_score.get(id));
        return (aux == null) ? -1 : aux.doubleValue();
    }


    /**
     *  Gets the authorityScoreFromID attribute of the Related object
     *
     *@param  id  Description of the Parameter
     *@return     The authorityScoreFromID value
     */
    public double getAuthorityScoreFromID(Long id) {
        Double aux = (Double) (url_authority_score.get(id));
        return (aux == null) ? -1 : aux.doubleValue();
    }


    /**
     *  Gets the hubScore attribute of the Related object
     *
     *@param  url  Description of the Parameter
     *@return      The hubScore value
     */
    public double getHubScore(String url) {
        Long id = (Long) (url_to_id.get(new Long(((Object) (url.toString())).hashCode())));
        Double aux = (Double) (url_hub_score.get(id));
        return (aux == null) ? -1 : aux.doubleValue();
    }


    /**
     *  Gets the hubScoreFromID attribute of the Related object
     *
     *@param  id  Description of the Parameter
     *@return     The hubScoreFromID value
     */
    public double getHubScoreFromID(Long id) {
        Double aux = (Double) (url_hub_score.get(id));
        return (aux == null) ? -1 : aux.doubleValue();
    }


    /**
     *  Gets the inLinks attribute of the Related object
     *
     *@param  url  Description of the Parameter
     *@return      The inLinks value
     */
    public List getInLinks(String url2) {
		String url = url2;
		if (!url.startsWith("http://")) url = "http://"+ url;
        Vector v1 = new Vector();
        Long id = null;
        try {
			Statement stmt = null;
			ResultSet rset = null;
			stmt = conn.createStatement();
			rset = stmt.executeQuery("select id from tumbafculpt where url='"+url+"'");
			if (rset.next()) {
				id = new Long(rset.getLong(1)); 
				stmt.close();
			} else { 
				stmt.close();
				return v1;
			}
			stmt = conn.createStatement();
			rset = stmt.executeQuery("select pai from ligacoes_pesos where filho=" + id + " and rownum <= " + maxLinks + " order by peso");
            while (rset.next()) {
                v1.addElement(new Long(rset.getLong(1)));
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
		logger.debug("Found " + v1.size() + " results for inlinks query with id " + id);
        return v1;
    }


    /**
     *  Gets the outLinks attribute of the Related object
     *
     *@param  url  Description of the Parameter
     *@return      The outLinks value
     */
    public List getOutLinks(String url2) {
		String url = url2;
        if (!url.startsWith("http://")) url = "http://"+ url;
        Vector v1 = new Vector();
		Long id = null;
        try {
			Statement stmt = null;
			ResultSet rset = null;
			stmt = conn.createStatement();
			rset = stmt.executeQuery("select id from tumbafculpt where url='"+url+"'");
			if (rset.next()) {
				id = new Long(rset.getLong(1)); 
				stmt.close();
			} else { 
				stmt.close();
				return v1;
			} 
			stmt = conn.createStatement();
			rset = stmt.executeQuery("select filho from ligacoes_pesos where pai=" + id + " and rownum <= " + maxLinks + " order by peso");
			while (rset.next()) {
				v1.addElement(new Long(rset.getLong(1)));
			}
			rset.close();
			stmt.close();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
		logger.debug("Found " + v1.size() + " results for outlinks query with id "+id);
        return v1;
    }


    /**
     *  Gets the related attribute of the Related object
     *
     *@param  url2  Description of the Parameter
     *@return       The related value
     */
    public List getRelated(String url2) {
        loadLinks(url2);
        compute();
        Iterator it;
		if(cocitations.size()==0) {
			return new Vector();
		}
        Long url = (Long) (id_to_url.get((Long) (cocitations.keySet().iterator().next())));
        Long id = (Long) (url_to_id.get(url));
        HashMap urls = (HashMap) (cocitations.get(id));
        HashMap urls2 = (HashMap) (couplings.get(id));
        if (urls == null) {
            return new Vector();
        }
        List v = new Vector();
        it = urls.keySet().iterator();
        while (it.hasNext()) {
            Long id3 = (Long) (it.next());
            v.add(id3);
            urls.put(id3, new Double(-(((Double) (urls.get(id3))).doubleValue() * 0.7 + ((Double) (urls2.get(id3))).doubleValue() * 0.2 + getAuthorityScoreFromID(id3) * 0.1)));
        }
        if (v.size() < 2) return new Vector();
        Stack stack = new Stack();
        int low = 0;
        int high = v.size() - 1;
        int p;
        int i;
        int pivotIndex;
        int value;
        double ind1;
        double ind2;
        double ind3;
        double ind4;
        stack.push(new Integer(high));
        stack.push(new Integer(low));
        if (high <= 0) {
            return v;
        }
        while (!stack.empty()) {
            low = ((Integer) stack.pop()).intValue();
            high = ((Integer) stack.pop()).intValue();
            value = rand.nextInt();
            pivotIndex = low + (((value < 0) ? -1 * value : value) % (high + 1 - low));
            v = exchangePos(v, low, pivotIndex);
            for (p = low, i = low + 1; i <= high; i += 1) {
                ind1 = ((Double) (urls.get(v.get(i)))).doubleValue();
                ind2 = ((Double) (urls.get(v.get(low)))).doubleValue();
                ind3 = -getAuthorityScoreFromID((Long) (v.get(i)));
                ind4 = -getAuthorityScoreFromID((Long) (v.get(low)));
                if (ind1 < ind2 || (ind1 == ind2 && ind3 < ind4)) {
                    v = exchangePos(v, ++p, i);
                }
            }
            v = exchangePos(v, low, p);
            if (low < (p - 1)) {
                stack.push(new Integer(p - 1));
                stack.push(new Integer(low));
            }
            if ((p + 1) < high) {
                stack.push(new Integer(high));
                stack.push(new Integer(p + 1));
            }
        }
        
        
        for (i=0; i < v.size() && i<maxLinks; ) {
            if (((Double) (urls.get(v.get(i)))).doubleValue() < -0.0 && !v.get(i).equals(id)) {
                logger.debug("Related Page with rank --- " + i + " -- " + id_to_url.get(v.get(i)) + " -- "+ ((Double) (urls.get(v.get(i)))).doubleValue());
                v.set(i, id_to_url.get(v.get(i)));
                i++;
            } else {
                v.remove(i);
            }
        }
        while (v.size()>maxLinks) v.remove(i);
		logger.debug("Found " + v.size() + " results for related pages query with url " +url2);
        return v;
    }


    /**
     *  Description of the Method
     *
     *@param  url  Description of the Parameter
     */
    public void loadLinks(String url2) {
        String url = url2.toLowerCase();
		boolean useStop = true;

        if (!url.startsWith("http://")) url = "http://" + url;
        if(StopURLs.isStopURL(url)) useStop = false;
        try {
			Statement stmt = null;
            ResultSet rset = null;
            Long id = null;
            stmt = conn.createStatement();
           	rset = stmt.executeQuery("select id from tumbafculpt where url='"+url+"'");
           	if (rset.next()) {
            	id = new Long(rset.getLong(1)); 
           		rset.close();
           		stmt.close();
           	} else { 
				rset.close();
           		stmt.close();
           		return;
           	} 
            			            
			maxLevels = 1;            			            
            logger.debug("iniciou leitura links para related pages...");
            addURL(id, true);
			stmt = conn.createStatement();
			rset = stmt.executeQuery(escreveQuery(false, maxLevels, id.longValue()));
            while (rset.next()) {
                long l1 = rset.getLong(1);
				long l2 = rset.getLong(2);
                logger.debug("Adding Links " + l2 + " <- " + l1);
                addURL(new Long(l2), new Long(l1));
            }
            rset.close();
			stmt.close();

			maxLevels = 1;
			stmt = conn.createStatement();
			rset = stmt.executeQuery(escreveQuery(true, maxLevels, id.longValue()));
			while (rset.next()) {
				long l1 = rset.getLong(1);
				long l2 = rset.getLong(2);
				logger.debug("Adding Links " + l1 + " <- " + l2);
				addURL(new Long(l1), new Long(l2));
			}
			rset.close();
			stmt.close();
		
            logger.debug(" -- termoinou leitura links para related pages -- ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
