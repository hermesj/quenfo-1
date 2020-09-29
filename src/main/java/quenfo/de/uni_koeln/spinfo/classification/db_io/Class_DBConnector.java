package quenfo.de.uni_koeln.spinfo.classification.db_io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.core.data.DBMode;
import quenfo.de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;

public class Class_DBConnector {

	public static Connection connect(String dbFilePath) throws SQLException, ClassNotFoundException {
		Connection connection;
		// register the driver
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
		return connection;
	}
	
	public static void writeUnsplittedJobAds(Connection connection, Map<Integer, String> unsplitted) throws SQLException {
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("DROP TABLE IF EXISTS Unsplitted");
		
		String create = "CREATE TABLE Unsplitted" +
							"(ID INTEGER PRIMARY KEY AUTOINCREMENT, "
							+ " ZEILENNR INT NOT NULL, "
							+ " Jahrgang INT, "
							+ "LANG TEXT, "
							+ "STELLENBESCHREIBUNG Text NOT NULL)";
		
		stmt.executeUpdate(create);
		
		stmt.close();
		
		PreparedStatement prepStmt = connection.prepareStatement(
				"INSERT INTO Unsplitted (ZEILENNR, Jahrgang, LANG, STELLENBESCHREIBUNG) VALUES(?,?,?,?)");
		
		for (Map.Entry<Integer, String> e : unsplitted.entrySet()) {
			prepStmt.setInt(1, e.getKey());
			prepStmt.setInt(2, 2017);
			prepStmt.setString(3, "de");
			prepStmt.setString(4, e.getValue());
			
			prepStmt.addBatch();
		}
		
		prepStmt.executeBatch();
		prepStmt.close();
		
		
		connection.commit();
		
	}
	
	public static void dropOutput(Connection connection) throws SQLException {
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("DELETE FROM ClassifiedParagraphs");
		stmt.close();
		connection.commit();
	}


	public static void createClassificationOutputTables(Connection connection, DBMode dbMode)
			throws SQLException {
//		StringBuffer sql;
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement();
		if (dbMode.equals(DBMode.OVERWRITE)) {
			stmt.executeUpdate("DROP TABLE IF EXISTS ClassifiedParagraphs");
			connection.commit();
		}
			
//		sql = new StringBuffer("CREATE TABLE ClassifiedParagraphs" + "(ID INTEGER PRIMARY KEY AUTOINCREMENT , "
//				+ " JAHRGANG 	INT		NOT NULL, " + " POSTINGID	TEXT	NOT	NULL, TEXT TEXT, "
//				+ " ClassONE   	INT     NOT NULL, " + " ClassTWO    INT    	NOT NULL, "
//				+ " ClassTHREE  INT    	NOT NULL, " + " ClassFOUR  	INT    	NOT NULL)");
		
		String query = "CREATE TABLE ClassifiedParagraphs (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " JAHRGANG INT NOT NULL, "
				+ "POSTINGID TEXT NOT NULL, "
				+ "PARAGRAPHID INT UNIQUE NOT NULL, "
				+ "TEXT TEXT, "
				+ "ClassONE INT NOT NULL, "
				+ "ClassTWO INT NOT NULL, "
				+ "ClassTHREE INT NOT NULL, "
				+ "ClassFOUR INT NOT NULL)";

		stmt.executeUpdate(query);
		stmt.close();
		connection.commit();

	}

	public static void addColumn(Connection connection, String column, String table)
			throws SQLException {
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement();
		try{
			stmt.executeUpdate("ALTER TABLE "+table+" ADD " + column + " TEXT");
			stmt.close();
			connection.commit();
		}
		catch(SQLException e){
			//Spalte exisitiert bereits
		}
	}

	/**
	 * adds all paragraphs of one job ad to the output connection
	 * @param connection output connection (classifiedParagraphs.db)
	 * @param results splitted and classified paragraphs 
	 * @param jahrgang year of job ad
	 * @param postingID posting ID of job ad
	 * @return
	 */
	public static boolean insertClassifiedParagraphsinDB(Connection connection, List<ClassifyUnit> results,
			String jahrgang, String postingID) {
		boolean[] classIDs;
		int paraID = -1;
		try {
			connection.setAutoCommit(false);

			Statement stmt = connection.createStatement();
			PreparedStatement prepStmt;
			prepStmt = connection.prepareStatement(
					"INSERT INTO ClassifiedParagraphs (TEXT, JAHRGANG, POSTINGID, PARAGRAPHID, ClassONE,ClassTWO,ClassTHREE,ClassFOUR) VALUES(?,?,?,?,?,?,?,?)");
			for (ClassifyUnit cu : results) {
				int booleanRpl = 0; // replaces true/false for saving into sqliteDB
				paraID = cu.hashCode();
				classIDs = ((JASCClassifyUnit) cu).getClassIDs();
				prepStmt.setString(1, cu.getContent());
				prepStmt.setString(2, jahrgang);
				prepStmt.setString(3, postingID);
				prepStmt.setInt(4, paraID);
//				System.out.println(postingID);
				for (int classID = 0; classID <= 3; classID++) {
					if (classIDs[classID])
						booleanRpl = 1;	
					else
						booleanRpl = 0;
					
//					System.out.println(classID + " -> " + booleanRpl);
					prepStmt.setInt(5 + classID, booleanRpl);
				}
//				System.out.println();
				prepStmt.addBatch();
			}
			prepStmt.executeBatch();
			prepStmt.close();
			stmt.close();
			connection.commit();
			return true;
			
		} catch (SQLException e) {
			System.err.println("Fehler beim Schreiben von Stellenanzeige: " + postingID + " " +
		paraID + "\n" + e.getMessage());
			return false;
		}
			
	}


}
