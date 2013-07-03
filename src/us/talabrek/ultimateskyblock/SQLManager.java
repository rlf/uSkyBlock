package us.talabrek.ultimateskyblock;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.MySQL;

@SuppressWarnings("unused")
public class SQLManager {

	private MySQL sql;
	public final Logger logger;
	public final String prefix, hostname, database, username, password;
	public final int port;
	private boolean connected;
	private static final String setup = "username VARCHAR(15) PRIMARY KEY";
	private static final String table = "data";

	/**
	 * A simple MySQL tool for ease of access.
	 * 
	 * @param logger
	 *            The plugin logger
	 * @param prefix
	 *            Prefix for SQL output
	 * @param hostname
	 *            Host
	 * @param port
	 *            Port
	 * @param database
	 *            Database
	 * @param username
	 *            Username
	 * @param password
	 *            Password
	 */
	public SQLManager(Logger logger, String prefix, String hostname, int port, String database, String username, String password) {
		this.logger = logger;
		this.prefix = prefix;
		this.hostname = hostname;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		logger.info("Connecting...");
		connect();
		setup();
	}

	/**
	 * Establish connection with MySQL.
	 */
	public void connect() {
		try {
			sql = new MySQL(logger, prefix, hostname, port, database, username, password);
			sql.open();
			if (sql.isOpen()) {
				logger.info("Connected.");
				connected = true;
			} else {
				logger.warning("Failed to connect.");
				connected = false;
			}
		} catch (Exception e) {
			logger.warning(e.getMessage());
			logger.warning("Failed to connect.");
			connected = false;
		}
	}

	/**
	 * Disconnect from MySQL.
	 */
	public void disconnect() {
		if (sql != null && connected) {
			logger.info("Disconnected.");
			sql.close();
		}
	}

	/**
	 * @return true if connected to MySQL.
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Create tables.
	 */
	public void setup() {
		if (!isConnected()) { return; }
		try {
			logger.info("Setting up MySQL.");
			query("CREATE TABLE IF NOT EXISTS " + table + " (" + setup + ")");
		} catch (Exception e) {
		}
	}

	/**
	 * Empty the given table.
	 * 
	 * @param table
	 *            The table name
	 * @return true if deletion was successful
	 */
	public boolean emptyTable(String table) {
		return query("TRUNCATE TABLE " + table);
	}

	/**
	 * Add a player to the table if they're not currently in.
	 * 
	 * @param user
	 *            The username to add
	 */
	public void add(String user) {
		if (!isIn(table, user)) {
			query("INSERT INTO " + table + " (username) VALUES ('" + user + "')");
		}
	}

	/**
	 * Query a MySQL command
	 * 
	 * @param cmd
	 *            The command
	 * @return If the command executed properly
	 */
	private boolean query(String cmd) {
		if (!isConnected()) { return false; }
		try {
			sql.query(cmd);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Query a MySQL command and receive a ResultSet
	 * 
	 * @param cmd
	 *            The command
	 * @return The ResultSet or null if the command failed.
	 */
	private ResultSet queryResponse(String cmd) {
		if (!isConnected()) { return null; }
		try {
			ResultSet result = sql.query(cmd);
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Determine whether or not a given username is in the table.
	 * 
	 * @param table
	 *            The table name
	 * @param name
	 *            The username to check
	 * @return true if the user is in the table.
	 */
	private boolean isIn(String table, String name) {
		if (!isConnected()) { return false; }
		try {
			return queryResponse("SELECT * FROM " + table + " WHERE username = '" + name + "' LIMIT 1").next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Get a list of all primary keys in the table.
	 * 
	 * @param table
	 *            The table name
	 * @return A list of all the primary keys, an empty list if none found or
	 *         null if not connected.
	 */
	public List<String> getKeys(String table) {
		if (!isConnected()) { return null; }
		List<String> list = new ArrayList<String>();
		try {
			ResultSet rs = queryResponse("SELECT * FROM " + table + " WHERE username != 'null'");
			if (rs != null) {
				while (rs.next()) {
					if (rs.getString("username") != null) {
						list.add(rs.getString("username"));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Run a query to set data in MySQL.
	 * 
	 * @param table
	 *            The table
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @param value
	 *            The value
	 */
	public void set(String table, String name, String field, Object value) {
		query("UPDATE " + table + " SET " + field + " = " + value + " WHERE username = '" + name + "' LIMIT 1");
	}

	/**
	 * Get a piece of data out of the MySQL database.
	 * 
	 * @param table
	 *            The table
	 * @param name
	 *            The primary key
	 * @param field
	 *            The field
	 * @return The object received or null if nothing found
	 */
	public Object get(String table, String name, String field) {
		try {
			ResultSet rs = queryResponse("SELECT * FROM " + table + " WHERE username = '" + name + "' LIMIT 1");
			if (rs.next()) { return rs.getObject(field); }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
