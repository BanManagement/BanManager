package me.confuserr.banmanager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableMap;

public class Database {
	protected enum Statements {
		SELECT, INSERT, UPDATE, DELETE, DO, REPLACE, LOAD, HANDLER, CALL, // Data
																			// manipulation
																			// statements
		CREATE, ALTER, DROP, TRUNCATE, RENAME // Data definition statements
	}

	private String user;
	private String pass;
	private String url;
	public Logger log;
	private Connection connection;
	private BanManager plugin;
	private boolean queryInProgress = false;
	private ImmutableMap<String, String> tables;

	public Database(String user, String pass, String url, BanManager instance, Map<String, String> tables) {
		this.user = user;
		this.pass = pass;
		this.url = url;
		plugin = instance;
		
		this.tables = ImmutableMap.copyOf(tables);
	}

	private boolean initialize() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return true;
		} catch (ClassNotFoundException e) {
			plugin.getLogger().severe("MySQL driver class missing: " + e.getMessage() + ".");
			return false;
		}
	}
	
	public String getTable(String name) {
		return tables.get(name);
	}

	public boolean checkConnection() {
		return open() != null;
	}

	public Connection open() {
		if (!initialize())
			return null;
		try {
			if (connection == null)
				return DriverManager.getConnection(this.url, this.user, this.pass);
			else if (connection.isValid(0)) // Check the connection is valid
				return connection;
			else {
				// Return a new connection!
				return DriverManager.getConnection(this.url, this.user, this.pass);
			}
		} catch (SQLException e) {
			plugin.getLogger().severe(this.url);
			plugin.getLogger().severe("Could not be resolved because of an SQL Exception: " + e.getMessage() + ".");
		}
		return null;
	}

	public void close() {
		if (queryInProgress)
			return;

		connection = open();
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
		} catch (Exception e) {
			plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
		}
	}

	public ResultSet query(String query) {
		Statement statement;
		ResultSet result = null;
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
			plugin.getLogger().warning("Error in SQL query: " + e.getMessage());
			plugin.getLogger().warning(query);
			Util.sendMessageWithPerm("Error in SQL query: " + e.getMessage(), "bm.sqlnotify");
		}
		return result;
	}

	public int updateQuery(String query) {
		Connection connection;
		Statement statement;
		try {
			connection = open();
			statement = connection.createStatement();
			return statement.executeUpdate(query);
		} catch (SQLException e) {
			plugin.getLogger().warning("Error in SQL query: " + e.getMessage());
			plugin.getLogger().warning(query);
		}
		return 0;
	}

	public PreparedStatement prepare(String query) {
		Connection connection;
		PreparedStatement ps = null;
		try {
			connection = open();
			ps = connection.prepareStatement(query);
			return ps;
		} catch (SQLException e) {
			if (!e.toString().contains("not return ResultSet")) {
				plugin.getLogger().warning("Error in SQL prepare() query: " + e.getMessage());
				plugin.getLogger().warning(query);
			}
		}
		return ps;
	}

	protected Statements getStatement(String query) {
		String trimmedQuery = query.trim();
		if (trimmedQuery.substring(0, 6).equalsIgnoreCase("SELECT"))
			return Statements.SELECT;
		else if (trimmedQuery.substring(0, 6).equalsIgnoreCase("INSERT"))
			return Statements.INSERT;
		else if (trimmedQuery.substring(0, 6).equalsIgnoreCase("UPDATE"))
			return Statements.UPDATE;
		else if (trimmedQuery.substring(0, 6).equalsIgnoreCase("DELETE"))
			return Statements.DELETE;
		else if (trimmedQuery.substring(0, 6).equalsIgnoreCase("CREATE"))
			return Statements.CREATE;
		else if (trimmedQuery.substring(0, 5).equalsIgnoreCase("ALTER"))
			return Statements.ALTER;
		else if (trimmedQuery.substring(0, 4).equalsIgnoreCase("DROP"))
			return Statements.DROP;
		else if (trimmedQuery.substring(0, 8).equalsIgnoreCase("TRUNCATE"))
			return Statements.TRUNCATE;
		else if (trimmedQuery.substring(0, 6).equalsIgnoreCase("RENAME"))
			return Statements.RENAME;
		else if (trimmedQuery.substring(0, 2).equalsIgnoreCase("DO"))
			return Statements.DO;
		else if (trimmedQuery.substring(0, 7).equalsIgnoreCase("REPLACE"))
			return Statements.REPLACE;
		else if (trimmedQuery.substring(0, 4).equalsIgnoreCase("LOAD"))
			return Statements.LOAD;
		else if (trimmedQuery.substring(0, 7).equalsIgnoreCase("HANDLER"))
			return Statements.HANDLER;
		else if (trimmedQuery.substring(0, 4).equalsIgnoreCase("CALL"))
			return Statements.CALL;
		else
			return Statements.SELECT;
	}

	public boolean createTable(String query) {
		Statement statement;
		try {
			this.connection = this.open();
			if (query.equals("")) {
				plugin.getLogger().severe("SQL query empty: createTable(" + query + ")");
				return false;
			}
			statement = connection.createStatement();
			statement.execute(query);
			return true;
		} catch (SQLException e) {
			plugin.getLogger().severe(e.getMessage());
			return false;
		} catch (Exception e) {
			plugin.getLogger().severe(e.getMessage());
			return false;
		}
	}

	public boolean checkTable(String table) {
		try {
			connection = open();
			// this.connection = this.open();
			if (connection == null) {
				plugin.getLogger().severe("Unable to check if tables exist");
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
				plugin.getLogger().info("Error in SQL query: " + e.getMessage());
			}
		}

		return query("SELECT * FROM " + table) == null;
	}

	public boolean colExists(String table, String column) {
		try {
			connection = open();
			// this.connection = this.open();
			if (connection == null) {
				plugin.getLogger().severe("Unable to check if tables exist");
				return false;
			}

			DatabaseMetaData metadata = connection.getMetaData();

			ResultSet result = metadata.getColumns(null, null, table, column);
			if (result == null)
				return false;
			if (result != null) {
				if (result.next()) {
					result.close();
					return true;
				}
			}
		} catch (SQLException e) {
			if (e.getMessage().contains("exist")) {
				return false;
			} else {
				plugin.getLogger().info("Error in SQL query: " + e.getMessage());
			}
		}

		return false;
	}
}