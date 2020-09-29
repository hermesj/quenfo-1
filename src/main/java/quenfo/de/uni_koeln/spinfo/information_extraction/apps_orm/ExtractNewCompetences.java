package quenfo.de.uni_koeln.spinfo.information_extraction.apps_orm;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import quenfo.de.uni_koeln.spinfo.core.helpers.PropertiesHandler;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.IEType;
import quenfo.de.uni_koeln.spinfo.information_extraction.workflow.ORMExtractor;

public class ExtractNewCompetences {
	
	static IEType ieType;

	// Pfad zur ORM-Datenbank
	static String dbFilePath;
	
	// txt-File mit allen bereits bekannten (validierten) Kompetenzen (die
		// bekannten Kompetenzn helfen beim Auffinden neuer Kompetenzen)
		static File competences;

		// txt-File mit bekannten (typischen) Extraktionsfehlern (würden ansonsten
		// immer wieder vorgeschlagen werden)
		static File noCompetences;

		// txt-File mit den Extraktionspatterns
		static File compPatterns;

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
			
			ORMExtractor extractor = new ORMExtractor(jobadConnection, competences, noCompetences, compPatterns, modifier,
					ieType, expandCoordinates);
			extractor.extract(startPos, queryLimit, fetchSize);
			
			// TODO startPos & queryLimit pruefen
			
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
			
			ieType = PropertiesHandler.getSearchType("ie");

			dbFilePath = quenfoData + "/sqlite/orm/" + PropertiesHandler.getStringProperty("general", "orm_database");
			
			queryLimit = PropertiesHandler.getIntProperty("ie", "queryLimit");
			startPos = PropertiesHandler.getIntProperty("ie", "startPos");
			fetchSize = PropertiesHandler.getIntProperty("ie", "fetchSize");
			expandCoordinates = PropertiesHandler.getBoolProperty("ie", "expandCoordinates");
			
			
			String competencesFolder = quenfoData + "/resources/information_extraction/competences/";
			competences = new File(competencesFolder + PropertiesHandler.getStringProperty("ie", "competences"));
			noCompetences = new File(competencesFolder + PropertiesHandler.getStringProperty("ie", "noCompetences"));
			modifier = new File(competencesFolder + PropertiesHandler.getStringProperty("ie", "modifier"));
			compPatterns = new File(competencesFolder + PropertiesHandler.getStringProperty("ie", "compPatterns"));
			

			
		}

}
