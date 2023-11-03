package pengu.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

/**
 * This class allows for the communication of MS Access files to be made.
 * @author Shivun Chinniah
 */
public class PenguAccessDBCommunicator {

    /**
     * Parameterized constructor method.
     * @param dbAddress The address of the database file.
     */
    public PenguAccessDBCommunicator(String dbAddress) {
        this.dbAddress = dbAddress;

    }

    /**
     * Stores the address of the database file.
     */
    protected String dbAddress;
    

    /**
     * Gets Current System time in milliseconds.
     *
     * @return Returns long representation of current time.
     */
    public static long now() {

        return Calendar.getInstance().getTimeInMillis();
    }

    
    /**
     * Used for making SELECT queries to the database file.
     * 
     * @param sql The SQL statement to execute.
     * @return Returns a ResultSet of the results from the query.
     */
    protected ResultSet query(String sql) {

        ResultSet rs = null;
        Connection con;
        try {
            String url = "jdbc:ucanaccess://" + dbAddress;
            con = DriverManager.getConnection(url);

            Statement stmt = con.createStatement();
            rs = stmt.executeQuery(sql);
            
            con.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return rs;
    }

    /**
     * Used for making UPDATE, INSERT, and DELETE queries to the database file.
     * Note:<br>
     * There is no protection against SQL injections using this method.
     * 
     * @param sql The SQL statement to execute.
     * @return Returns an integer count of effected rows.
     */
    protected int queryUpdate(String sql) {
        int result = 0;
        Connection con;
        try {
            String url = "jdbc:ucanaccess://" + dbAddress;
            con = DriverManager.getConnection(url);

            Statement stmt = con.createStatement();
            result = stmt.executeUpdate(sql);
            
            con.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        } 
        return result;
    }

     

    /**
     * Makes a Connection to the target database.
     * 
     * @return Returns a Connection object that has been connected to the target database file. 
     */
    protected Connection getConnectionInstance() {
        try {
            Connection con;
            String url = "jdbc:ucanaccess://" + dbAddress;
            con = DriverManager.getConnection(url);
            return con;
        } catch (SQLException ex) {
            System.out.println(ex + " @ getConnectionInstance");
            return null;
            
        }
    }

    

}
