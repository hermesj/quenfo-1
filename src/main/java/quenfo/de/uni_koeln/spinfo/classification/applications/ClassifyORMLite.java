package quenfo.de.uni_koeln.spinfo.classification.applications;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import quenfo.de.uni_koeln.spinfo.classification.core.classifier.AbstractClassifier;
import quenfo.de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import quenfo.de.uni_koeln.spinfo.classification.core.data.DBMode;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import quenfo.de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import quenfo.de.uni_koeln.spinfo.classification.core.distance.Distance;
import quenfo.de.uni_koeln.spinfo.classification.core.feature_engineering.feature_weighting.AbstractFeatureQuantifier;
import quenfo.de.uni_koeln.spinfo.classification.core.feature_engineering.feature_weighting.LogLikeliHoodFeatureQuantifier;
import quenfo.de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.jasc.workflow.ORMDatabaseClassifier;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.classifier.ZoneKNNClassifier;
import quenfo.de.uni_koeln.spinfo.core.helpers.PropertiesHandler;

public class ClassifyORMLite {
	
	static String resourcesDB = "jdbc:sqlite:C:\\quenfo_data/resources/models.db";
	
	static String dbFilePath = null;

	// Pfad zum Output-Ordner in dem die neue DB angelegt werden soll
	static String outputFolder = null;

	// Pfad zur Datei mit den Trainingsdaten
	static String trainingdataFile = null;

	// Anzahl der Stellenanzeigen, die klassifiziert werden sollen (-1 = gesamte
	// Tabelle)
	static int queryLimit;

	// falls nur eine begrenzte Anzahl von SteAs klassifiziert werden soll
	// (s.o.): hier die Startosition angeben
	static int startPos;

	// Die SteAs werden (aus Speichergründen) nicht alle auf einmal ausgelesen,
	// sondern Päckchenweise - hier angeben, wieviele jeweils in einem Schwung
	// zusammen verarbeitet werden
	// nach dem ersten Schwung erscheint in der Konsole ein Dialog, in dem man
	// das Programm nochmal stoppen (s), die nächsten xx SteAs klassifizieren
	// (c), oder ohne Unterbrechung zu Ende klassifizieren lassen kann (d)
	static int fetchSize;

	static boolean normalize = false;

	static boolean stem = false;

	static boolean filterSW = false;

	static int[] nGrams = null;

	static boolean continousNGrams = false;

	static int miScore = 0;

	static boolean suffixTree = false;
	
	static DBMode dbMode;

	public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
		
		
		if (args.length > 0) {
			String configFolder = args[1];
			try {
				dbMode = DBMode.valueOf(args[2].toUpperCase());
			} catch (RuntimeException e) { //IllegalArgumentException oder ArrayIndexOutOfBoundsException
				System.out.println("No Database Mode set. Append results to existing DB.\n"
						+ "To choose mode add 'overwrite' or 'append' on last position to command line interface.");
				dbMode = DBMode.APPEND;
			}
			
			loadProperties(configFolder);
		}

		// dbFilePath = "C:\\quenfo_data/sqlite/jobads/ormlite_test.db";
//		String trainingDataFileName = "";

		String databaseUrl = "jdbc:sqlite:" + dbFilePath;
		// create a connection source to our database
		ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);
		
		
		
		ConnectionSource resourcesConnection = new JdbcConnectionSource(resourcesDB);
		
		if (dbMode.equals(DBMode.OVERWRITE)) {
			try {
				TableUtils.clearTable(connectionSource, JASCClassifyUnit.class);
			} catch (SQLException e) {
				System.err.println("Noch keine Daten zum Überschreiben vorhanden.");
			}
		}
			
		
		
		long before = System.currentTimeMillis();
		ORMDatabaseClassifier ormClassifier = new ORMDatabaseClassifier(connectionSource, queryLimit, fetchSize,
				startPos, trainingdataFile);

		FeatureUnitConfiguration fuc = new FeatureUnitConfiguration(normalize, stem, filterSW, nGrams, continousNGrams,
				miScore, suffixTree);
		AbstractFeatureQuantifier fq = new LogLikeliHoodFeatureQuantifier();
		AbstractClassifier classifier = new ZoneKNNClassifier(false, 5, Distance.COSINUS);
		ExperimentConfiguration config = new ExperimentConfiguration(fuc, fq, classifier, new File(trainingdataFile),
				outputFolder);
		
		Dao<? extends Model, ?> modelDao = classifier.getModelDao(resourcesConnection);
		
		
		

		if (!modelDao.isTableExists())
				TableUtils.createTable(modelDao);
		
		List<? extends Model> trainedModels = classifier.getPersistedModels(config.hashCode());
		
//		if (classifier instanceof ZoneKNNClassifier)
//			List<ZoneKNNModel> trainedModels = (List<ZoneKNNModel>) modelDao.queryForEq("configHash", configHash);
//		System.out.println(trainedModels);
		
		if (trainedModels.isEmpty()) {
			ormClassifier.trainAndClassify(config, modelDao);
			// Model trainieren und persistieren
			
		} else {
			
			// mit trainiertem Model klassifizieren
			ormClassifier.classify(trainedModels.get(0), config);
			
			
			
		}
		//TODO falls trainedModels, nur classify, sonst train and classify

		
		long after = System.currentTimeMillis();
		double time = (((double) after - before) / 1000) / 60;
		if (time > 60) {
			System.out.println("\nfinished Classification in " + (time / 60) + "hours");
		} else {
			System.out.println("\nfinished Classification in " + time + " minutes");
		}
		connectionSource.close();
		resourcesConnection.close();

	}
	
	
	private static void loadProperties(String folderPath) throws IOException {

		File configFolder = new File(folderPath);

		if (!configFolder.exists()) {
			System.err.println("Config Folder " + folderPath + " does not exist."
					+ "\nPlease change configuration and start again.");
			System.exit(0);
		}
		
		String quenfoData = configFolder.getParent();
		PropertiesHandler.initialize(configFolder);
		
		dbFilePath = quenfoData + "/sqlite/orm/" + PropertiesHandler.getStringProperty("general", "orm_database");
//		inputDB = quenfoData + "/sqlite/jobads/" + PropertiesHandler.getStringProperty("general", "jobAdsDB");
//		inputTable = PropertiesHandler.getStringProperty("general", "jobAds_inputTable");
		outputFolder = quenfoData + "/sqlite/classification/";
//		outputDB = PropertiesHandler.getStringProperty("general", "classifiedParagraphs");
		
		trainingdataFile = quenfoData + "/resources/classification/trainingSets/" + PropertiesHandler.getStringProperty("classification", "trainingDataFile");
		
		startPos = PropertiesHandler.getIntProperty("classification", "startPos");
		queryLimit = PropertiesHandler.getIntProperty("classification", "queryLimit");
		fetchSize = PropertiesHandler.getIntProperty("classification", "fetchSize");

		normalize = PropertiesHandler.getBoolProperty("classification", "normalize");
		stem = PropertiesHandler.getBoolProperty("classification", "stem");
		filterSW = PropertiesHandler.getBoolProperty("classification", "filterSW");
		nGrams = PropertiesHandler.getIntArrayProperty("classification", "nGrams");
		continousNGrams = PropertiesHandler.getBoolProperty("classification", "continousNGrams");

	}

}
