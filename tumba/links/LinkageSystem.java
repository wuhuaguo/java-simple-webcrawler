package pt.tumba.links;

import java.sql.*;

/**
 * Get the Web graph from a relational database
 * The expected format of the table holding the linkage information is as follows:
 * 
 * CREATE TABLE links (
 *                                    link_id int(11) NOT NULL auto_increment,
 *                                    id_page_from int(11) NOT NULL,
 *                                    id_page_to int(11) NOT NULL
 * 	                             );
 *                                
 * @author Bruno Martins
 */
public class LinkageSystem {

    /** The JDBC Connection String for connecting to the relational database */
    private String db_url           = "jdbc:mysql://myserver/link_db";

    /** The username for connecting to the relational database */
    private String db_user          = "";

    /** The password for connecting to the relational database */
    private String db_password      = "";


    /** The JDBC Driver for connecting to the relational database */
    private String db_driver_name   = "org.gjt.mm.mysql.Driver";
    
    /**
     * Default constructor for LinkageSystem.
     * The default values for the JDBC connection parameters are used.
     */ 
    public LinkageSystem() {
    }
    
    /**
     * Constructor for LinkageSystem
     *
     * @param p_db_url The JDBC Connection String for connecting to the relational database
     * @param p_db_driver_name The JDBC Driver for connecting to the relational database
     * @param p_db_user The username for connecting to the relational database
     * @param p_db_password The password for connecting to the relational database
     */ 
    public LinkageSystem (String p_db_url, String p_db_driver_name,String p_db_user, String p_db_password) {
        this.setDBURL(p_db_url);
        this.setDBDriverName(p_db_driver_name);
        this.setDBUser(p_db_url);
        this.setDBPassword(p_db_driver_name);

    }
    
    /**
     * Sets the value for the JDBC Connection String used for connecting to the relational database
     *
     * @param p_db_url The JDBC Connection String for connecting to the relational database
     */
    public void setDBURL(String p_db_url) {
        this.db_url = p_db_url;
    }

    /**
     * Sets the value for the JDBC Driver used for connecting to the relational database
     *
     * @param p_db_driver_name The JDBC Driver for connecting to the relational database
     */
    public void setDBDriverName(String p_db_driver_name) {
        this.db_driver_name = p_db_driver_name;
    }

    /**
     * Sets the value for the username used for connecting to the relational database
     *
     * @param p_db_user The username for connecting to the relational database
     */
    public void setDBUser(String p_db_user) {
        this.db_user = p_db_user;
    }

    /**
     * Sets the value for the password used for connecting to the relational database
     *
     * @param p_db_password The password for connecting to the relational database
     */
    public void setDBPassword(String p_db_password) {
        this.db_password = p_db_password;
    }

    /**
     * Returns the JDBC Connection String used for connecting to the relational database
     *
     * @return The JDBC Connection String for connecting to the relational database
     */
    public String getDBURL() {
        return this.db_url;
    }

    /**
     * Returns the JDBC Driver used for connecting to the relational database
     *
     * @return The JDBC Driver for connecting to the relational database
     */
    public String getDBDriverName() {
        return this.db_driver_name;
    }
    
    /**
     * Returns the username used for connecting to the relational database
     *
     * @return The username for connecting to the relational database
     */
    public String getDBUser() {
        return this.db_user;
    }

    /**
     * Returns the password used for connecting to the relational database
     *
     * @return The password for connecting to the relational database
     */
    public String getDBPassword() {
        return this.db_password;
    }

    /**
     * Returns a string with statistics gathered from the linkage database.
     * These statistics include a list of the pages in the database, together
     * with the pages they link to.
     */
    public String getInfo() {
        String main_select = "SELECT * from pages";
        StringBuffer res = new StringBuffer("List : \n ------------------- \n");
        try {
            Class.forName(db_driver_name);
            Connection con = DriverManager.getConnection(db_url, "USERNAME", "PASSWORD");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(main_select);
            String sel_query   = "SELECT q.name " +
                                 "       FROM pages as p, links as l, pages as q " +
                                 "       WHERE p.page_id = l.id_page_from AND " +
                                 "             q.page_id = l.id_page_to AND " +
                                 "             p.page_id = ? ";
            PreparedStatement stmt2 = con.prepareStatement(sel_query);
            while (rs.next()) {
                res.append("(\"" + rs.getString("name") + "\" [" + rs.getDouble("pr") + "]) -->");
                stmt2.setString(1, rs.getString("page_id"));
                ResultSet rs2 = stmt2.executeQuery();
                while (rs2.next()) {
                    res.append(" (\"" + rs2.getString("q.name") + "\")");
                }
                res.append("\n");
            }
        } catch (SQLException ex) {
            System.out.println("\n*** SQLException caught ***\n");
            while (ex != null) {
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("Message:  " + ex.getMessage());
                System.out.println("Vendor:   "   + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        }
        return res.toString();
    }
  
}
