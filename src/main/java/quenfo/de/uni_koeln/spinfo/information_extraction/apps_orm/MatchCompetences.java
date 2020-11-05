package quenfo.de.uni_koeln.spinfo.information_extraction.apps_orm;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import quenfo.de.uni_koeln.spinfo.classification.core.data.DBMode;
import quenfo.de.uni_koeln.spinfo.core.helpers.PropertiesHandler;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.IEType;
import quenfo.de.uni_koeln.spinfo.information_extraction.workflow.ORMExtractor;

public class MatchCompetences {

	static IEType ieType;

	// Pfad zur ORM-Datenbank
	static String dbFilePath;

	// txt-File mit den validierten Kompetenzen
	static File notCatComps;

	// tei-File mit kategorisierten Kompetenzen
	static File catComps;

	// Ebene, auf der die Kompetenz zugeordnet werden soll(div1, div2, div3, entry,
	// form, orth)
	static String category;

	// txt-File mit allen 'Modifier'-Ausdrücken
	static File modifier;

	// txt-File zur Speicherung der Match-Statistiken
	static File statisticsFile;

	// Anzahl der Paragraphen aus der Input-DB, gegen die gematcht werden soll
	// (-1 = alle)
	static int queryLimit;

	// Falls nicht alle Paragraphen gematcht werden sollen, hier die
	// Startposition angeben
	static int startPos;

	static int fetchSize;

	// true, falls Koordinationen in Informationseinheit aufgelöst werden sollen
	static boolean expandCoordinates;

	static DBMode dbMode;

	public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {

		if (args.length > 0) {
			String configPath = args[1];
			try {
				dbMode = DBMode.valueOf(args[2].toUpperCase());
			} catch (RuntimeException e) { // IllegalArgumentException oder ArrayIndexOutOfBoundsException
				System.out.println("No Database Mode set. Append results to existing DB.\n"
						+ "To choose mode add 'overwrite' or 'append' to command line interface.");
				dbMode = DBMode.APPEND;
			}
			loadProperties(configPath);
		}

		// create connection to jobad sqlite database
				String databaseUrl = "jdbc:sqlite:" + dbFilePath;
				ConnectionSource jobadConnection = new JdbcConnectionSource(databaseUrl);
				
				if (dbMode.equals(DBMode.OVERWRITE)) {
					// TODO Tabelle überschreiben
				}

				long before = System.currentTimeMillis();

				ORMExtractor extractor = new ORMExtractor(jobadConnection, notCatComps, modifier,
						ieType, expandCoordinates);
				extractor.stringMatch(startPos, queryLimit, fetchSize);

				long after = System.currentTimeMillis();
				Double time = (((double) after - before) / 1000) / 60;
				if (time > 60.0) {
					System.out.println("\nfinished Competence-Extraction in " + (time / 60) + " hours");
				} else {
					System.out.println("\nFinished Competence-Extraction in " + time + " minutes");
				}
	}

	private static void loadProperties(String folderPath) throws IOException {

		File configFolder = new File(folderPath);

		if (!configFolder.exists()) {
			System.err.println("Config Folder " + folderPath + " does not exist."
					+ "\nPlease change configuration and start again.");
			System.exit(0);
		}

		// initialize and load all properties files
		String quenfoData = configFolder.getParent();
		PropertiesHandler.initialize(configFolder);

		// get values from properties files

		ieType = PropertiesHandler.getSearchType("matching");

		dbFilePath = quenfoData + "/sqlite/orm/" + PropertiesHandler.getStringProperty("general", "orm_database");
		
		queryLimit = PropertiesHandler.getIntProperty("matching", "queryLimit");
		startPos = PropertiesHandler.getIntProperty("matching", "startPos");
		fetchSize = PropertiesHandler.getIntProperty("matching", "fetchSize");
		expandCoordinates = PropertiesHandler.getBoolProperty("matching", "expandCoordinates");

		String competencesFolder = quenfoData + "/resources/information_extraction/competences/";
		notCatComps = new File(competencesFolder + PropertiesHandler.getStringProperty("matching", "competences"));
		modifier = new File(competencesFolder + PropertiesHandler.getStringProperty("matching", "modifier"));

		statisticsFile = new File(
				competencesFolder + PropertiesHandler.getStringProperty("matching", "compMatchingStats"));


		ieType = PropertiesHandler.getSearchType("ie");

		dbFilePath = quenfoData + "/sqlite/orm/" + PropertiesHandler.getStringProperty("general", "orm_database");

		queryLimit = PropertiesHandler.getIntProperty("ie", "queryLimit");
		startPos = PropertiesHandler.getIntProperty("ie", "startPos");
		fetchSize = PropertiesHandler.getIntProperty("ie", "fetchSize");
		expandCoordinates = PropertiesHandler.getBoolProperty("ie", "expandCoordinates");



	}

}
