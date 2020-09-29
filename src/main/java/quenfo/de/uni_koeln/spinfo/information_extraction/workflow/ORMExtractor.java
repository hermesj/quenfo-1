package quenfo.de.uni_koeln.spinfo.information_extraction.workflow;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

import is2.lemmatizer.Lemmatizer;
import is2.tag.Tagger;
import is2.tools.Tool;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.helpers.SingleToMultiClassConverter;
import quenfo.de.uni_koeln.spinfo.core.helpers.PropertiesHandler;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.IEType;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.InformationEntity;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.Pattern;
import quenfo.de.uni_koeln.spinfo.information_extraction.preprocessing.ExtractionUnitBuilder;

public class ORMExtractor {

	private Logger log = LogManager.getLogger();

	private IEJobs jobs;
	private IEType type;

	ConnectionSource connection;

	// beide Variablen werden nur gebraucht, wenn Validierungen (0 & 1) aus Tabelle
	// gelesen werden
//	private Set<String> knownEntities = new HashSet<String>();
//	private Set<String> noEntities = new HashSet<String>();
	private Map<String, String> possCompoundSplits = new HashMap<String, String>();
	private File possCompoundsFile, splittedCompoundsFile;
//	private File entitiesFile;
//	private File noEntitiesFile;

	ConnectionSource ormConnection;

	/**
	 * Constructor to extract new competences / tools
	 * 
	 * @param ormConnection
	 * @param entitiesFile
	 * @param noEntitiesFile
	 * @param compPatterns
	 * @param modifier
	 * @param ieType
	 * @param expandCoordinates
	 * @throws IOException
	 */
	public ORMExtractor(ConnectionSource ormConnection, File entitiesFile, File noEntitiesFile, File contexts,
			File modifier, IEType ieType, boolean resolveCoordinations) throws IOException {
		// TODO Auto-generated constructor stub
//		this.entitiesFile = entitiesFile;
//		this.noEntitiesFile = noEntitiesFile;
		this.type = ieType;
		this.possCompoundsFile = new File(PropertiesHandler.getPossibleCompounds());
		this.splittedCompoundsFile = new File(PropertiesHandler.getSplittedCompounds());
		this.jobs = new IEJobs(entitiesFile, noEntitiesFile, modifier, contexts, type, resolveCoordinations,
				possCompoundsFile, splittedCompoundsFile);

		this.connection = ormConnection;

		initialize();
	}

	private void initialize() {
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
		JASCClassifyUnit.setNumberOfCategories(stmc.getNumberOfCategories(), stmc.getNumberOfClasses(),
				stmc.getTranslations());
	}

	public void extract(int queryOffset, int queryLimit, int fetchSize) throws SQLException, IOException {
		// TODO Auto-generated method stub
		Tool lemmatizer = new Lemmatizer(PropertiesHandler.getLemmatizerModel(), false);
		Tool tagger = new Tagger(PropertiesHandler.getTaggerModel());

		Dao<JASCClassifyUnit, String> cuDao = DaoManager.createDao(connection, JASCClassifyUnit.class);
		Dao<ExtractionUnit, String> exuDao = DaoManager.createDao(connection, ExtractionUnit.class);
		if (!exuDao.isTableExists())
			TableUtils.createTable(exuDao);

		QueryBuilder<JASCClassifyUnit, String> queryBuilder = cuDao.queryBuilder();

		List<JASCClassifyUnit> classifyUnits = null;
		List<ExtractionUnit> extractionUnits = null;
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> extractions = null;
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> allExtractions = new HashMap<ExtractionUnit, Map<InformationEntity, List<Pattern>>>();

		PreparedQuery<JASCClassifyUnit> prepQuery;

		int cuCount = 0;

		while (cuCount < queryLimit) {

			// pruefen, ob fetchsize uber querylimit hinaus geht
			if ((cuCount + fetchSize) > queryLimit)
				fetchSize = queryLimit - cuCount;
			log.info("classifying {} job ads, skipping first {} rows", fetchSize, queryOffset);

			// TODO where class = 3 / 2
			prepQuery = queryBuilder.offset((long) queryOffset).orderBy("id", true).limit((long) fetchSize).prepare();
			classifyUnits = cuDao.query(prepQuery);

			cuCount += classifyUnits.size();

			// FIXME extract from classify unit

			// Paragraphen in Sätze splitten und in ExtractionUnits überführen
			log.info("initialize ExtractionUnits");
			extractionUnits = ExtractionUnitBuilder.initializeIEUnits(classifyUnits, lemmatizer, null, tagger);
			log.info("--> " + extractionUnits.size());
			
			//TEST
			exuDao.create(extractionUnits);
			//TESt

			// Informationsextraktion
			jobs.annotateTokens(extractionUnits);
			log.info("extract " + type.name().toLowerCase());
			extractions = jobs.extractEntities(extractionUnits, lemmatizer);

			possCompoundSplits.putAll(jobs.getNewCompounds());

			// Entfernen der bereits bekannten Entitäten
			extractions = removeKnownEntities(extractions);

			for (Map.Entry<ExtractionUnit, Map<InformationEntity, List<Pattern>>> e : extractions.entrySet()) {
				System.out.println(e.getValue().keySet());
			}
			allExtractions.putAll(extractions);

			queryOffset += fetchSize;

		}

	}

	private Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> removeKnownEntities(
			Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> allExtractions) {
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> toReturn = new HashMap<ExtractionUnit, Map<InformationEntity, List<Pattern>>>();
		for (ExtractionUnit extractionUnit : allExtractions.keySet()) {

			Map<InformationEntity, List<Pattern>> ies = allExtractions.get(extractionUnit);
			Map<InformationEntity, List<Pattern>> filterdIes = new HashMap<InformationEntity, List<Pattern>>();
			for (InformationEntity ie : ies.keySet()) {
				Set<InformationEntity> knownIEs = jobs.entities.get(ie.getStartLemma());
				if (knownIEs == null || (!knownIEs.contains(ie))) {
					filterdIes.put(ie, ies.get(ie));
				}
			}
			if (!filterdIes.isEmpty()) {
				toReturn.put(extractionUnit, filterdIes);
			}
		}
		return toReturn;
	}

}
