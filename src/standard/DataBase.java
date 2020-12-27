package standard;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;								  								   
import org.hsqldb.util.DatabaseManagerSwing;


public class DataBase {
	
	private String propertyFile = null;
	private String ActivityDB   = null;
	private String TaskDB       = null;
	private String JDBCDriver   = null;
	private String Connection   = null;
	private String Username     = null;
	private String Password     = null;
	
	public DataBase(String propertyFile) {
		this.propertyFile = propertyFile;
	}
	
    public Map<String, List<String>> initiateDate () {
	    Connection con = null;
	    Statement stmt = null;
	    
	    Map<String, List<String>> Activity = new HashMap<>();
	    List<String> Task;
		
		getProperties();
			    
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

	private void getProperties() {
		
		Properties prop = new Properties();
		
		InputStream in;
		try {
			in = new FileInputStream(propertyFile);
			if (in != null) {
				prop.loadFromXML(in);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ActivityDB = prop.getProperty("ActivityDB");
        TaskDB     = prop.getProperty("TaskDB");
        JDBCDriver = prop.getProperty("JDBCDriver");
        Connection = prop.getProperty("Connection");
        Username   = prop.getProperty("Username");
        Password   = prop.getProperty("Password");
	}

    private Connection connectDB () throws ClassNotFoundException, SQLException {
    	
		Class.forName(JDBCDriver);
		return DriverManager.getConnection(Connection, Username, Password);
    }
      
    public Integer writeDB (String sql) throws ClassNotFoundException, SQLException {

        Connection con = connectDB();
        Statement stmt = con.createStatement();
        Integer update = stmt.executeUpdate(sql);
        con.close();
		return update;
    }
    
    private void initDatabase (Statement stmt) throws SQLException {

        // Create tables if not exists
        String sql = "CREATE TABLE IF NOT EXISTS Activity ("
    	   	+ "ActivityName longvarchar NOT NULL PRIMARY KEY,"
    		+ "isActive boolean NOT NULL)";

        Integer createTable = stmt.executeUpdate(sql);
    
        sql = "CREATE TABLE IF NOT EXISTS Task (TaskID INTEGER IDENTITY PRIMARY KEY,"
    		+ "	TaskName longvarchar NOT NULL,"
    		+ "	ActivityName longvarchar NOT NULL,"
    		+ "	isActive boolean NOT NULL,"
    		+ " FOREIGN KEY (ActivityName) REFERENCES Activity(ActivityName))";
        
        createTable = stmt.executeUpdate(sql);
    
        sql = "CREATE TABLE IF NOT EXISTS TimeSpend (TimeSpendID INTEGER IDENTITY PRIMARY KEY,"
    		+ "Description longvarchar NOT NULL,"
    		+ "ActivityName longvarchar NOT NULL,"
    		+ "TaskID integer,"
    		+ "DATE date NOT NULL,"
    		+ "StartTime time NOT NULL,"
    		+ "EndTime time NOT NULL,"
    		+ "isActive boolean NOT NULL,"
			+ "isLogging boolean NOT NULL,"
    	    +    "FOREIGN KEY (ActivityName) REFERENCES Activity(ActivityName),"
    	    +    "FOREIGN KEY (TaskID) REFERENCES Task(TaskID))";
    
        createTable = stmt.executeUpdate(sql);
     	
    }

    private static void loadInitialData (Connection con, String file) throws IOException, SQLException {
        Statement stmt = con.createStatement();
        BufferedReader br = null;
        try {
 	       br = new BufferedReader(new FileReader(file));
 	       String sql = br.readLine();
 	       while (sql != null) {
 	           stmt.executeQuery(sql);
 	           sql = br.readLine();
 	       }
 	   } catch (FileNotFoundException e) {
 	       e.printStackTrace();
 	   } finally {
 		   br.close();
 	   }
    }
		
	private Integer getID (String Table, String clause) throws ClassNotFoundException, SQLException {
		
		Connection con = connectDB();
    	Statement stmt = con.createStatement();
		
		String column = null;
		Integer ID = -1;
		
		String sql = "SELECT Column_Name  FROM information_schema.columns WHERE Table_Name = '" + Table.toUpperCase() + "' and Is_Identity = 'YES'";
		ResultSet rs = stmt.executeQuery(sql);

		while (rs.next()) {
			column = rs.getString("Column_name");
		}
		
		if (column != null) { 
		    sql = "SELECT " + column + " FROM " + Table + " WHERE " + clause;
		    rs = stmt.executeQuery(sql);

		    while (rs.next()) {
			   ID = rs.getInt(column);
		    }
		}
		return ID;
	}
	
	public Time[] getTimeSpend (Integer ID) throws ClassNotFoundException, SQLException{
		
        Connection con = connectDB();
        Statement stmt = con.createStatement();
		
		Time[] time = new Time[2];
		
		String sql = "SELECT StartTime, EndTime FROM TimeSpend WHERE TimeSpendID = " + ID;
		
		ResultSet rs = stmt.executeQuery(sql);
		while (rs.next()){
			time[0] = rs.getTime("StartTime");
			time[1] = rs.getTime("EndTime");
		}
		
		con.close();
		return time;
	}

    public Integer closeActivityTask (String Activity, String Task) throws ClassNotFoundException, SQLException {

        String strTaskID = "TimeSpend.TaskID IS NULL";
        if (Task != null) {
            strTaskID = "TimeSpend.TaskID = (SELECT TaskID FROM TASK WHERE Taskname = '" + Task + "')";
        }
        
        String sql = "UPDATE TimeSpend SET EndTime = '" + LocalTime.now() + "', isActive = false " + 
      		     "WHERE ActivityName =  '" + Activity + "' and " +
      		     "Date = '" + LocalDate.now() + "' and " + 
      		     strTaskID + " and " + 
      		     "isActive = true;";
				 
		Integer TimeSpendID = getID("TimeSpend", "isActive = true and ActivityName = '" + Activity + "' and " + strTaskID);
				 
		writeDB(sql);
		
		return TimeSpendID;

    }

    public String[] checkOpenActivityTask () throws ClassNotFoundException, SQLException {
    	
    	Connection con = connectDB();
    	Statement stmt = con.createStatement();
		
		String[] str = new String[3];
    	
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

    public void openDBManager () throws ClassNotFoundException, SQLException {
        String[] args = { "--user", "SA", "--url", "jdbc:hsqldb:file:./data/Timer"};
        DatabaseManagerSwing.main(args);    	
    }
}
