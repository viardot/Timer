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
import java.time.Duration;

import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hsqldb.util.DatabaseManagerSwing;


public class DataBase {
	

	private String propertyFile = null;	
	private String ActivityDB = null;
	private String TaskDB     = null;
	private String JDBCDriver = null;
	private String Connection = null;
	private String Username   = null;
	private String Password   = null;

					   
 
									
									
									
									
									
									
									
 
	public DataBase(String propertyFile) {
		this.propertyFile = propertyFile;
		getProperties();
	}
	
	public Map<String, List<String>> initiateData () {	
	    Connection con = null;
	    Statement stmt = null;
		
		Map<String, List<String>> Activity = new HashMap<>();
	    List<String> Task;
		
				  
	   
        try {
			
			con = connectDB();
			
						 
	  
			if (con != null) {
				
				stmt = con.createStatement();
				
				String sql ="SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_NAME = 'ACTIVITY'";
			    
				ResultSet rs = getResultSet(con, sql);
				
				//  change initDatabase such that is excepts Connection
	   
											 
	   
			    if (!rs.next()) {
			    	initDatabase(con);
			    	// Fill the database with content
			    	loadInitialData(con, ActivityDB);
			    	loadInitialData(con, TaskDB);
			    }
				
				// Close open Activities more than a day old. Do not use writeDB because that will openen a new connection to the db.
			    sql = "UPDATE TimeSpend SET isActive = false WHERE isActive = true and Date <> '" + LocalDate.now() + "'";
			    
			    Integer updateOldRecords = stmt.executeUpdate(sql);			 
				
			    // Query the database
			    sql = "SELECT ActivityName FROM Activity WHERE isActive = true";

				rs = getResultSet(con, sql);
                
			    while (rs.next()) {
			    	Task = new ArrayList<>();
			    	Activity.put(rs.getString("ActivityName"), Task);
			    }
				
				sql = "SELECT ActivityName, TaskName FROM Activity, Task WHERE Activity.ActivityName = Task.ActivityName and Task.isActive = true ORDER BY ActivityName"; 
				rs = getResultSet(con, sql);
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

    private final void getProperties() {
		
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
	
	private void initDatabase (Connection con) throws SQLException {

        Statement stmt = con.createStatement();
		
	   
										 
												   
											 
						
						 
					   
		 
				
	 
	
																	

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

    private void loadInitialData (Connection con, String file) throws IOException, SQLException {
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
											
		
		String column = null;
		Integer ID = -1;
		
		String sql = "SELECT Column_Name  FROM information_schema.columns WHERE Table_Name = '" + Table.toUpperCase() + "' and Is_Identity = 'YES'";
		ResultSet rs = getResultSet(con, sql);

		while (rs.next()) {
			column = rs.getString("Column_name");
		}
		
		if (column != null) { 
		    sql = "SELECT " + column + " FROM " + Table + " WHERE " + clause;
			rs = getResultSet(con, sql);

		    while (rs.next()) {
			   ID = rs.getInt(column);
		    }
		}
		return ID;
	}

    private ResultSet getResultSet (Connection con, String sql) throws SQLException {

		return con.createStatement().executeQuery(sql);
	
	}

    public Integer writeDB (String sql) {
		
		Connection con = null;
		Integer update = -1;
        
		try {
            con = connectDB();
            Statement stmt = con.createStatement();
            update = stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
        } finally {
			try {
			    con.close();
			} catch (SQLException se) { 
			   se.printStackTrace(); 
			}
		}
		return update;
    }
	
	public Duration getTimeSpend (Integer ID){
		
        Connection con = null;
		Time[] time = new Time[2];
		
		try {
    	    con = connectDB();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (con != null) {
			
			String sql = "SELECT StartTime, EndTime FROM TimeSpend WHERE TimeSpendID = " + ID;
			try {
													  
			   ResultSet rs = getResultSet(con, sql);
		       while (rs.next()){
			       time[0] = rs.getTime("StartTime");
			       time[1] = rs.getTime("EndTime");
		        }
			} catch (SQLException se) {
				se.printStackTrace();
			} finally {
				try {
				    con.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		}
        
		Duration d = Duration.between(time[0].toLocalTime(), time[1].toLocalTime());
		
		return d;
	}

    public Integer closeActivityTask (String Activity, String Task) {

        String strTaskID = "TimeSpend.TaskID IS NULL";
		Integer TimeSpendID = -1;
		
        if (Task != null) {
            strTaskID = "TimeSpend.TaskID = (SELECT TaskID FROM TASK WHERE Taskname = '" + Task + "')";
        }
        
        String sql = "UPDATE TimeSpend SET EndTime = '" + LocalTime.now() + "', isActive = false " + 
      		     "WHERE ActivityName =  '" + Activity + "' and " +
      		     "Date = '" + LocalDate.now() + "' and " + 
      		     strTaskID + " and " + 
      		     "isActive = true;";
				
		try {
		    TimeSpendID = getID("TimeSpend", "isActive = true and ActivityName = '" + Activity + "' and " + strTaskID);
		} catch (Exception e) {
			e.printStackTrace();
		}
				 
		writeDB(sql);
		
		return TimeSpendID;

    }

    public String[] checkOpenActivityTask () {
		
		Connection con = null;
        String[] str = new String[3];
    	
		try {
    	    con = connectDB();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (con != null) {

    	    String sql = "SELECT ActivityName, Task.TaskName, Description " +
    			     "FROM TimeSpend LEFT JOIN Task ON TimeSpend.TaskID = Task.TaskID " +
    			     "WHERE isActive= true and Date = '"+ LocalDate.now() +"' and StartTime = (SELECT max(StartTime) FROM TimeSpend)";
    	
		    try {
													
				ResultSet rs = getResultSet(con, sql);
    	        while (rs.next()) {
    		        str[0] = rs.getString(1);
    		        str[1] = rs.getString(2);
    		        str[2] = rs.getString(3);
    	        }
                con.close();
		    } catch (SQLException se) {
			    se.printStackTrace();
		    }
		}
        
    	return str;
    }

    public void openDBManager () {
        String[] args = { "--user", "SA", "--url", "jdbc:hsqldb:file:./data/Timer"};
		try {
            DatabaseManagerSwing.main(args);
		} catch (Exception e){
			e.printStackTrace();
		}			
    }
}
