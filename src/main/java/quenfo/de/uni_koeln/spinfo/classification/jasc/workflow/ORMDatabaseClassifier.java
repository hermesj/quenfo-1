package quenfo.de.uni_koeln.spinfo.classification.jasc.workflow;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import quenfo.de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import quenfo.de.uni_koeln.spinfo.classification.core.helpers.EncodingProblemTreatment;
import quenfo.de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.jasc.preprocessing.ClassifyUnitSplitter;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.classifier.RegexClassifier;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.helpers.SingleToMultiClassConverter;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.workflow.ZoneJobs;
import quenfo.de.uni_koeln.spinfo.core.data.JobAd;
import quenfo.de.uni_koeln.spinfo.core.helpers.PropertiesHandler;

public class ORMDatabaseClassifier {

	private Logger log = LogManager.getLogger();

	private ConnectionSource connection;

	private ZoneJobs jobs;

	private long queryLimit, fetchSize, queryOffset;

	private String trainingDataFileName;

	public ORMDatabaseClassifier(ConnectionSource connection, int queryLimit, int fetchSize, int offset,
			String trainingDataFileName) throws IOException {
		this.connection = connection;

		this.queryLimit = queryLimit;
		this.fetchSize = fetchSize;
		this.queryOffset = offset;
		this.trainingDataFileName = trainingDataFileName;

		// set Translations
		Map<Integer, List<Integer>> translations = new HashMap<Integer, List<Integer>>();
		List<Integer> categories = new ArrayList<Integer>();
		categories.add(1);
		categories.add(2);
		translations.put(5, categories);
		categories = new ArrayList<Integer>();
		categories.add(2);
		categories.add(3);
		translations.put(6, categories);
		SingleToMultiClassConverter stmc = new SingleToMultiClassConverter(6, 4, translations);
		jobs = new ZoneJobs(stmc);
	}

	public Model train(ExperimentConfiguration config, Dao<? extends Model, ?> modelDao) throws IOException, SQLException {
		
		log.info("...training...");
		// get trainingdata from file (and db)
		File trainingDataFile = new File(trainingDataFileName);
		List<ClassifyUnit> trainingData = new ArrayList<ClassifyUnit>();

		trainingData.addAll(jobs.getCategorizedParagraphsFromFile(trainingDataFile,
				config.getFeatureConfiguration().isTreatEncoding()));

		if (trainingData.size() == 0) {
			System.out.println(
					"\nthere are no training paragraphs in the specified training-DB. \nPlease check configuration and try again");
			System.exit(0);
		}
		log.info("training paragraphs: " + trainingData.size());

		trainingData = jobs.initializeClassifyUnits(trainingData);
		log.info("Configuration: " + config.getFeatureConfiguration());
		trainingData = jobs.setFeatures(trainingData, config.getFeatureConfiguration(), true);
		trainingData = jobs.setFeatureVectors(trainingData, config.getFeatureQuantifier(), null);

		// build model
		Dao<Model, ? > castedModelDao = (Dao<Model, ?>) modelDao;
		Model model = jobs.getNewModelForClassifier(trainingData, config);
		try {
			castedModelDao.create(model);
		} catch (SQLException e) {
			System.err.println("Modell mit diesen Konfiguationen bereits persistiert.");
		}
		
		return model;
		
		
		//modelDao.create(model);
//		classify(model, config);

	}

	public void classify(Model model, ExperimentConfiguration config) throws IOException, SQLException {
//		log.info(model.getClass().getName());
		log.info("...classifying...");

		RegexClassifier regexClassifier = new RegexClassifier(PropertiesHandler.getRegex());
		// get data from db

		// instantiate the dao
		Dao<JobAd, String> jobAdDao = DaoManager.createDao(connection, JobAd.class);

		Dao<JASCClassifyUnit, String> cuDao = DaoManager.createDao(connection, JASCClassifyUnit.class);
		if (!cuDao.isTableExists())
			TableUtils.createTable(cuDao);

		QueryBuilder<JobAd, String> queryBuilder = jobAdDao.queryBuilder();

		
		
		while(queryOffset < queryLimit) {
			// FIXME fetchSize on ORMClassification
			
			// ...
			
			// queryOffset += fetchSize
			
			
		}
		
		PreparedQuery<JobAd> prepQuery = queryBuilder.offset(queryOffset).limit(queryLimit).where().eq("language", "de")
				.prepare();
		List<JobAd> jobAds = jobAdDao.query(prepQuery);

		for (JobAd jobad : jobAds) {
			List<JASCClassifyUnit> paragraphs = classifyJobad(jobad, config, model, regexClassifier);
			try {
				cuDao.create(paragraphs);
			} catch(SQLException e) {
				System.err.println("Abschnitt von Anzeige " + jobad.getPostingID() + " bereits in DB enthalten.");
			}
			
		}

	}

	private List<JASCClassifyUnit> classifyJobad(JobAd job, ExperimentConfiguration config, Model model,
			RegexClassifier regexClassifier) throws IOException {

		// 1. Split into paragraphs and create a ClassifyUnit per paragraph
		Set<String> paragraphs = ClassifyUnitSplitter.splitIntoParagraphs(job.getContent());

		// if treat enc
		if (config.getFeatureConfiguration().isTreatEncoding()) {
			paragraphs = EncodingProblemTreatment.normalizeEncoding(paragraphs);
		}
		List<ClassifyUnit> classifyUnits = new ArrayList<ClassifyUnit>();
		for (String string : paragraphs) {
			classifyUnits.add(new JASCClassifyUnit(string, job.getJahrgang(), job.getPostingID()));
		}
		// prepare ClassifyUnits
		classifyUnits = jobs.initializeClassifyUnits(classifyUnits);
		classifyUnits = jobs.setFeatures(classifyUnits, config.getFeatureConfiguration(), false);
		classifyUnits = jobs.setFeatureVectors(classifyUnits, config.getFeatureQuantifier(), model.getFUOrder());
		
//		double[] vec;
//		for(ClassifyUnit cu : classifyUnits) {
//			vec = cu.getFeatureVector();
//			for(int i = 0; i < vec.length; i++)
//				System.out.print(vec[0] + " ");
//			System.out.println();
//		}
			

		// 2. Classify
//		RegexClassifier regexClassifier = new RegexClassifier(PropertiesHandler.getRegex());
		Map<ClassifyUnit, boolean[]> preClassified = new HashMap<ClassifyUnit, boolean[]>();
		for (ClassifyUnit cu : classifyUnits) {
			boolean[] classes = regexClassifier.classify(cu, model);
			preClassified.put(cu, classes);
		}

		Map<ClassifyUnit, boolean[]> classified = jobs.classify(classifyUnits, config, model);

		classified = jobs.mergeResults(classified, preClassified);
		classified = jobs.translateClasses(classified);

		List<JASCClassifyUnit> results = new ArrayList<>();
		for (ClassifyUnit cu : classified.keySet()) {
			JASCClassifyUnit jcu = ((JASCClassifyUnit) cu);
			jcu.setClassIDs(classified.get(cu));
			results.add(jcu);
			
//			log.info(jcu.transformToClassID());
//			for (boolean b : jcu.getClassIDs())
//				System.out.print(b + " ");
//			System.out.println();
		}

		return results;

	}

}
