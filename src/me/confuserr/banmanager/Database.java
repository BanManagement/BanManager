package me.confuserr.banmanager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class Database {
	protected enum Statements {
		SELECT, INSERT, UPDATE, DELETE, DO, REPLACE, LOAD, HANDLER, CALL, // Data manipulation statements
		CREATE, ALTER, DROP, TRUNCATE, RENAME  // Data definition statements
	}
	
	private String user;
	private String pass;
	private String url;
	public Logger log;
	private Connection connection;
	private BanManager plugin;
	private boolean queryInProgress = false;
	
	public Database(String user, String pass, String url, BanManager instance) {
		this.user = user;
		this.pass = pass;
		this.url = url;
		plugin = instance;
	}
	
	public boolean checkConnection() {
		if(open() == null)
			return false;
		return true;
	}
	
	public Connection open() {
		try {
			if(connection == null)
				return DriverManager.getConnection(this.url, this.user, this.pass);
			else
				return connection;
		} catch (SQLException e) {
			plugin.logger.severe(this.url);
			plugin.logger.severe("Could not be resolved because of an SQL Exception: " + e.getMessage() + ".");
		}
		return null;
	}
	public void close() {
		if(queryInProgress)
			return;
		
		connection = open();
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
		} catch (Exception e) {
			plugin.logger.severe("Failed to close database connection: " + e.getMessage());
		}
	}
	public ResultSet query(String query) {
		Statement statement = null;
		ResultSet result = null/*new JdbcRowSetImpl()*/;
		queryInProgress = true;
		try {
			connection = open();
		    statement = connection.createStatement();
		    
		    switch (this.getStatement(query)) {
			    case SELECT:
				    result = statement.executeQuery(query);
				    queryInProgress = false;
				    return result;
			    
			    default:
			    	statement.executeUpdate(query);
			    	queryInProgress = false;
			    	return result;
		    }
		} catch (SQLException e) {
			plugin.logger.info("Error in SQL query: " + e.getMessage());
		}
		return result;
	}
	public int updateQuery(String query) {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = open();
		    statement = connection.createStatement();
			   int result = statement.executeUpdate(query);
			   return result;
		} catch (SQLException e) {
			plugin.logger.info("Error in SQL query: " + e.getMessage());
		}
		return 0;
	}
	public PreparedStatement prepare(String query) {
		Connection connection = null;
		PreparedStatement ps = null;
		try
		{
			connection = open();
			ps = connection.prepareStatement(query);
			return ps;
		} catch(SQLException e) {
			if(!e.toString().contains("not return ResultSet"))
				plugin.logger.info("Error in SQL prepare() query: " + e.getMessage());
		}
		return ps;
	}
	
	protected Statements getStatement(String query) {
		String trimmedQuery = query.trim();
		if (trimmedQuery.substring(0,6).equalsIgnoreCase("SELECT"))
			return Statements.SELECT;
		else if (trimmedQuery.substring(0,6).equalsIgnoreCase("INSERT"))
			return Statements.INSERT;
		else if (trimmedQuery.substring(0,6).equalsIgnoreCase("UPDATE"))
			return Statements.UPDATE;
		else if (trimmedQuery.substring(0,6).equalsIgnoreCase("DELETE"))
			return Statements.DELETE;
		else if (trimmedQuery.substring(0,6).equalsIgnoreCase("CREATE"))
			return Statements.CREATE;
		else if (trimmedQuery.substring(0,5).equalsIgnoreCase("ALTER"))
			return Statements.ALTER;
		else if (trimmedQuery.substring(0,4).equalsIgnoreCase("DROP"))
			return Statements.DROP;
		else if (trimmedQuery.substring(0,8).equalsIgnoreCase("TRUNCATE"))
			return Statements.TRUNCATE;
		else if (trimmedQuery.substring(0,6).equalsIgnoreCase("RENAME"))
			return Statements.RENAME;
		else if (trimmedQuery.substring(0,2).equalsIgnoreCase("DO"))
			return Statements.DO;
		else if (trimmedQuery.substring(0,7).equalsIgnoreCase("REPLACE"))
			return Statements.REPLACE;
		else if (trimmedQuery.substring(0,4).equalsIgnoreCase("LOAD"))
			return Statements.LOAD;
		else if (trimmedQuery.substring(0,7).equalsIgnoreCase("HANDLER"))
			return Statements.HANDLER;
		else if (trimmedQuery.substring(0,4).equalsIgnoreCase("CALL"))
			return Statements.CALL;
		else
			return Statements.SELECT;
	}
	public boolean createTable(String query) {
		Statement statement = null;
		try {
			this.connection = this.open();
			if (query.equals("") || query == null) {
				plugin.logger.severe("SQL query empty: createTable(" + query + ")");
				return false;
			}
			statement = connection.createStatement();
		    statement.execute(query);
		    return true;
		} catch (SQLException e) {
			plugin.logger.severe(e.getMessage());
			return false;
		} catch (Exception e) {
			plugin.logger.severe(e.getMessage());
			return false;
		}
	}
	public boolean checkTable(String table) {
		try {
			connection = open();
			//this.connection = this.open();
			if(connection == null) {
				plugin.logger.severe("Unable to check if tables exist");
				return false;
			}
		    Statement statement = connection.createStatement();
		    
		    ResultSet result = statement.executeQuery("SELECT * FROM " + table);
		    
		    if (result == null)
		    	return false;
		    if (result != null)
		    	return true;
		} catch (SQLException e) {
			if (e.getMessage().contains("exist")) {
				return false;
			} else {
				plugin.logger.info("Error in SQL query: " + e.getMessage());
			}
		}
		
		
		if (query("SELECT * FROM " + table) == null) return true;
		return false;
	}
	
	public boolean colExists(String table, String column) {
		try {
			connection = open();
			//this.connection = this.open();
			if(connection == null) {
				plugin.logger.severe("Unable to check if tables exist");
				return false;
			}
			
			DatabaseMetaData metadata = connection.getMetaData();
		    
			ResultSet result = metadata.getColumns(null, null, table, column);
		    if (result == null)
		    	return false;
		    if (result != null) {
		    	if(result.next()) {
		    		result.close();
					return true;
		    	}
		    }
		} catch (SQLException e) {
			if (e.getMessage().contains("exist")) {
				return false;
			} else {
				plugin.logger.info("Error in SQL query: " + e.getMessage());
			}
		}

		return false;
	}
}