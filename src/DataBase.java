import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;
import org.hsqldb.util.DatabaseManagerSwing;


public class DataBase {
	
	private static final String ActivityDB = "./SQL/Timer_Activities_data.sql";
	private static final String TaskDB = "./SQL/Timer_Tasks_data.sql";
	
    public static Map<String, List<String>> initiateDate () {
	    Connection con = null;
	    Statement stmt = null;
	    
	    Map<String, List<String>> Activity = new HashMap<>();
	    List<String> Task;
	    
	    try {
			
	    	con = connectDB ();
	    	
			if (con != null) {
			    
			    stmt = con.createStatement();
			    // Check if a table Activity exists. If not create the tables Activity; Task; Timespend.
			    String sql ="SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_NAME = 'ACTIVITY'";
			    
			    ResultSet rs = stmt.executeQuery(sql);
			    
			    if (!rs.next()) {
			    	initDatabase(stmt);
			    	// Fill the database with content
			    	loadInitialData(con, ActivityDB);
			    	loadInitialData(con, TaskDB);
			    	
			       	// Removing the dummy entries
			       	sql = "DELETE FROM Task WHERE ActivityName = 'Dummy'";
			       	Integer deleteDummy = stmt.executeUpdate(sql);
			    	
			       	sql = "DELETE FROM Activity WHERE ActivityName = 'Dummy'";
			       	deleteDummy = stmt.executeUpdate(sql);
			    }
			    
			    // Close open Activities more than a day old
			    sql = "UPDATE TimeSpend SET isActive = false WHERE isActive = true and Date <> '" + LocalDate.now() + "'";
			    
			    Integer updateOldRecords = stmt.executeUpdate(sql);
			    
			    // Query the database
			    sql = "SELECT ActivityName FROM Activity WHERE isActive = true";

			    rs = stmt.executeQuery(sql);
                
			    while (rs.next()) {
			    	Task = new ArrayList<>();
			    	Activity.put(rs.getString("ActivityName"), Task);
			    }
			    
			    sql = "SELECT ActivityName, TaskName FROM Activity, Task WHERE Activity.ActivityName = Task.ActivityName and Task.isActive = true ORDER BY ActivityName"; 
			    rs = stmt.executeQuery(sql);
			    while(rs.next()) {
                  Activity.get(rs.getString("ActivityName")).add(rs.getString("TaskName"));			    	
			    }
			    rs.close();
			    
			} else {
			    System.out.println("Problem with creating connection");
			}
			      
	    } catch (Exception e) {
	        e.printStackTrace(System.out);
	    } finally {
	    	try { 
	    		if (stmt != null) con.close();
	    	} catch (SQLException se){
	    		//Do nothing
	    	}
	    	try {
	    		if (con != null) con.close();
	    	} catch (SQLException se){
	    		se.printStackTrace();
	    	}
	    }
	    return Activity;
     }

    private static Connection connectDB () throws ClassNotFoundException, SQLException {
    	final String JDBCDriver = "org.hsqldb.jdbc.JDBCDriver";
    	final String Connection = "jdbc:hsqldb:file:data/Timer";
    	final String UserName = "SA";
    	final String Password = "";
    	
		Class.forName(JDBCDriver);
		return DriverManager.getConnection(Connection, UserName, Password);
    }
      
    public static void writeDB (String sql) throws ClassNotFoundException, SQLException {

        Connection con = connectDB();
        Statement stmt = con.createStatement();
        Integer update = stmt.executeUpdate(sql);
        con.close();
    }
    
    private static void initDatabase (Statement stmt) throws SQLException {

        // Create tables if not exists
        String sql = "CREATE TABLE IF NOT EXISTS Activity ("
    	   	+ "ActivityName longvarchar NOT NULL PRIMARY KEY,"
    		+ "isActive boolean NOT NULL)";

        Integer createTable = stmt.executeUpdate(sql);
    
        sql = "CREATE TABLE IF NOT EXISTS Task (TaskID integer NOT NULL PRIMARY KEY,"
    		+ "	TaskName longvarchar NOT NULL,"
    		+ "	ActivityName longvarchar NOT NULL,"
    		+ "	isActive boolean NOT NULL,"
    		+ " FOREIGN KEY (ActivityName) REFERENCES Activity(ActivityName))";
        
        createTable = stmt.executeUpdate(sql);
    
        sql = "CREATE TABLE IF NOT EXISTS TimeSpend ("
    		+ "Description longvarchar NOT NULL,"
    		+ "ActivityName longvarchar NOT NULL,"
    		+ "TaskID integer,"
    		+ "DATE date NOT NULL,"
    		+ "StartTime time NOT NULL,"
    		+ "EndTime time NOT NULL,"
    		+ "isActive boolean NOT NULL,"
    	    +    "FOREIGN KEY (ActivityName) REFERENCES Activity(ActivityName),"
    	    +    "FOREIGN KEY (TaskID) REFERENCES Task(TaskID))";
    
        createTable = stmt.executeUpdate(sql);
    
        sql = "INSERT INTO Activity Values  ('Dummy', false)";
        Integer insertDummy = stmt.executeUpdate(sql);
    
        sql = "INSERT INTO Task Values (0, 'Dummy', 'Dummy', false)";
        insertDummy = stmt.executeUpdate(sql);

    	sql= "CREATE TRIGGER UniqueTaskKey\r\n" +
       	     "BEFORE INSERT on Task\r\n" + 
       	     "REFERENCING NEW AS newrow FOR EACH ROW\r\n" +
       	     "BEGIN ATOMIC\r\n" +
       	     "SET newrow.TaskID = SELECT MAX(TaskID)+1 FROM Task;\r\n" +
       	     "END";
       	Integer insertTrigger = stmt.executeUpdate(sql);       	
    }

    private static void loadInitialData (Connection con, String file) throws IOException, SqlToolError, SQLException {
    	SqlFile sf = new SqlFile(new File(file));
        sf.setConnection(con);
        sf.execute();
    }

    public static void closeActivityTask (String Activity, String Task) throws ClassNotFoundException, SQLException {

        String strTaskID = "TimeSpend.TaskID IS NULL";
        if (Task != null) {
            strTaskID = "TimeSpend.TaskID = (SELECT TaskID FROM TASK WHERE Taskname = '" + Task + "')";
        }
        
        String sql = "UPDATE TimeSpend SET EndTime = '" + LocalTime.now() + "', isActive = false " + 
      		     "WHERE ActivityName =  '" + Activity + "' and " +
      		     "Date = '" + LocalDate.now() + "' and " + 
      		     strTaskID + " and " + 
      		     "isActive = true;";

		writeDB(sql);

    }

    public static String[] checkOpenActivityTask () throws ClassNotFoundException, SQLException {
    	
    	Connection con = connectDB();
    	
    	String[] str = new String[3];
    	
    	Statement stmt = con.createStatement();
    	
    	String sql = "SELECT ActivityName, Task.TaskName, Description " +
    			     "FROM TimeSpend LEFT JOIN Task ON TimeSpend.TaskID = Task.TaskID " +
    			     "WHERE isActive= true and Date = '"+ LocalDate.now() +"' and StartTime = (SELECT max(StartTime) FROM TimeSpend)";
    	ResultSet rs = stmt.executeQuery(sql);
    	while (rs.next()) {
    		str[0] = rs.getString(1);
    		str[1] = rs.getString(2);
    		str[2] = rs.getString(3);
    	}
    		
    	con.close();
        
    	return str;
    }

    public static void openDBManager () throws ClassNotFoundException, SQLException {
        String[] args = { "--user", "SA", "--url", "jdbc:hsqldb:file:./data/Timer"};
        DatabaseManagerSwing.main(args);    	
    }
}
