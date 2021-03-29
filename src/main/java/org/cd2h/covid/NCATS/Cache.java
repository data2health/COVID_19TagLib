package org.cd2h.covid.NCATS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.uiowa.UMLS.Concept;

public class Cache {
	static Logger logger = Logger.getLogger(Cache.class);
	static boolean initialized = false;
	static boolean localInitialization = false;
	static String networkHostName = "deep-thought.slis.uiowa.edu";

	static {
		if (!logger.isAttached(null))
			PropertyConfigurator.configure("log4j.debug");
		try {
			logger.info("GeoNames initializing...");
			init();
			logger.info("GeoNames ready.");
		} catch (Exception e) {
			logger.error("error initalizing UMLS package", e);
		}
	}

	static synchronized void init() throws Exception {
		if (initialized)
			return;

		localInitialization = Runtime.getRuntime().availableProcessors() < 10;

		logger.info("\t loading UMLS...");
		Connection conn = getConnection(localInitialization);
		Concept.initialize(conn);
	}

	static Connection getConnection(boolean local) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		Class.forName("org.postgresql.Driver");
		Properties props = new Properties();
		props.setProperty("user", "eichmann");
		props.setProperty("password", "translational");
		if (local) {
			conn = DriverManager.getConnection("jdbc:postgresql://localhost/loki", props);
			PreparedStatement stmt = conn.prepareStatement("set search_path to geonames");
			stmt.execute();
			stmt.close();
			conn.setAutoCommit(false);
		} else {
//	    props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
//	    props.setProperty("ssl", "true");
			conn = DriverManager.getConnection("jdbc:postgresql://hal.local/cd2h", props);
		}
		return conn;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public boolean containsUMLSTerm(String term) {
		return Concept.isTerm(term);
	}

	public Vector<Concept> getConceptsByName(String name) {
		return Concept.getByTerm(name);
	}
}
