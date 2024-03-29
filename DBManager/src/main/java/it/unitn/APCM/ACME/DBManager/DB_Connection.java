package it.unitn.APCM.ACME.DBManager;

import it.unitn.APCM.ACME.ServerCommon.JDBC_Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * The type DB connection.
 * Used to connect to the SQLite database of files
 */
public class DB_Connection {
	/**
	 * The constant JDBC connection initialized with the ENV var 'DB_DBMANAGER'.
	 */
	private static final JDBC_Connection dbconn = new JDBC_Connection(System.getenv("DB_DBMANAGER"));
	/**
	 * The constant logger.
	 */
	private static final Logger log = LoggerFactory.getLogger(DB_Connection.class);

	/**
	 * Gets dbconn.
	 *
	 * @return the connection to the database file
	 */
	public static Connection getDbconn() {
		log.trace("Connection to db_files requested");
		return dbconn.getConn();
	}
}
