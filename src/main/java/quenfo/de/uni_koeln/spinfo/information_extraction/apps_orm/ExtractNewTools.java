package quenfo.de.uni_koeln.spinfo.information_extraction.apps_orm;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import quenfo.de.uni_koeln.spinfo.core.helpers.PropertiesHandler;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.IEType;
import quenfo.de.uni_koeln.spinfo.information_extraction.workflow.ORMExtractor;

public class ExtractNewTools {
	
	static IEType ieType = IEType.TOOL;

	// Pfad zur ORM-Datenbank
	static String dbFilePath;

	// txt-File mit allen bereits bekannten (validierten) Tools (die
	// bekannten Tools helfen beim Auffinden neuer Tools)
	static File tools;

	// txt-File mit bekannten (typischen) Extraktionsfehlern (würden ansonsten
	// immer wieder vorgeschlagen werden)
	static File noTools;

	// txt-File mit den Extraktionspatterns
	static File toolsPatterns;

	// txt-File mit bekannten modifiern ("vorausgesetzt" etc.)
	static File modifier;

	// falls nicht alle Paragraphen aus der Input-DB verwendet werden sollen:
	// hier Anzahl der zu lesenden Paragraphen festlegen
	// -1 = alle
	static int queryLimit;

	// falls nur eine bestimmte Anzahl gelesen werden soll, hier die startID
	// angeben
	static int startPos;

	static int fetchSize;

	// true, falls Koordinationen in Informationseinheit aufgelöst werden sollen
	static boolean expandCoordinates;

	public static void main(String[] args) throws IOException, SQLException {

		if (args.length > 0) {
			String configPath = args[1];
			loadProperties(configPath);
		}
		// ENHANCE append vs. overwrite

		// create connection to jobad sqlite database
		String databaseUrl = "jdbc:sqlite:" + dbFilePath;
		ConnectionSource jobadConnection = new JdbcConnectionSource(databaseUrl);

		// ENHANCE persist extraction patterns

		long before = System.currentTimeMillis();

		ORMExtractor extractor = new ORMExtractor(jobadConnection, tools, noTools, toolsPatterns, modifier,
				ieType, expandCoordinates);
		extractor.extract(startPos, queryLimit, fetchSize);

		long after = System.currentTimeMillis();
		Double time = (((double) after - before) / 1000) / 60;
		if (time > 60.0) {
			System.out.println("\nfinished Tools-Extraction in " + (time / 60) + " hours");
		} else {
			System.out.println("\nFinished Tools-Extraction in " + time + " minutes");
		}

	}
	
	private static void loadProperties(String folderPath) throws IOException {

		File configFolder = new File(folderPath);

		if (!configFolder.exists()) {
			System.err.println("Config Folder " + folderPath + " does not exist."
					+ "\nPlease change configuration and start again.");
			System.exit(0);
		}
		
		//initialize and load all properties files
		String quenfoData = configFolder.getParent();
		PropertiesHandler.initialize(configFolder);

		dbFilePath = quenfoData + "/sqlite/orm/" + PropertiesHandler.getStringProperty("general", "orm_database");
//		paraInputDB = quenfoData + "/sqlite/classification/" + PropertiesHandler.getStringProperty("general", "classifiedParagraphs");// + jahrgang + ".db";
		
		queryLimit = PropertiesHandler.getIntProperty("ie", "queryLimit");
		startPos = PropertiesHandler.getIntProperty("ie", "startPos");
		fetchSize = PropertiesHandler.getIntProperty("ie", "fetchSize");
		expandCoordinates = PropertiesHandler.getBoolProperty("ie", "expandCoordinates");
		
		String toolsFolder = quenfoData + "/resources/information_extraction/tools/";
		
		tools = new File(toolsFolder + PropertiesHandler.getStringProperty("ie", "tools"));
		noTools = new File(toolsFolder + PropertiesHandler.getStringProperty("ie", "noTools"));
		toolsPatterns = new File(toolsFolder + PropertiesHandler.getStringProperty("ie", "toolsPatterns"));

//		toolsIEOutputFolder = quenfoData + "/sqlite/information_extraction/tools/";
//		toolsIEOutputDB = PropertiesHandler.getStringProperty("ie", "toolsIEOutputDB");
	}

}
