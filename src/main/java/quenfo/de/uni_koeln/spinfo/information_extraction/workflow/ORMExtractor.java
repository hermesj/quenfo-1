package quenfo.de.uni_koeln.spinfo.information_extraction.workflow;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import is2.lemmatizer.Lemmatizer;
import is2.tag.Tagger;
import is2.tools.Tool;
import quenfo.de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.helpers.SingleToMultiClassConverter;
import quenfo.de.uni_koeln.spinfo.core.helpers.PropertiesHandler;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.IEType;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.InformationEntity;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.MatchedEntity;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.ExtractedEntity;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.Pattern;
import quenfo.de.uni_koeln.spinfo.information_extraction.preprocessing.ExtractionUnitBuilder;

public class ORMExtractor {

	private Logger log = LogManager.getLogger();

	private IEJobs jobs;
	private IEType type;

	ConnectionSource connection;

	private Map<String, String> possCompoundSplits = new HashMap<String, String>();
	private File possCompoundsFile, splittedCompoundsFile;

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

		this.type = ieType;
		this.possCompoundsFile = new File(PropertiesHandler.getPossibleCompounds());
		this.splittedCompoundsFile = new File(PropertiesHandler.getSplittedCompounds());
		this.jobs = new IEJobs(entitiesFile, noEntitiesFile, modifier, contexts, type, resolveCoordinations,
				possCompoundsFile, splittedCompoundsFile);

		this.connection = ormConnection;

		initialize();
	}

	/**
	 * Constructor to match known competences / tools
	 * 
	 * @param jobadConnection
	 * @param notCatComps
	 * @param modifier
	 * @param ieType
	 * @param expandCoordinates
	 * @throws IOException
	 */
	public ORMExtractor(ConnectionSource ormConnection, File entitiesFile, File modifier, IEType ieType,
			boolean resolveCoordinations) throws IOException {
		this(ormConnection, entitiesFile, null, null, modifier, ieType, resolveCoordinations);
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

		Tool lemmatizer = new Lemmatizer(PropertiesHandler.getLemmatizerModel(), false);
		Tool tagger = new Tagger(PropertiesHandler.getTaggerModel());

		Dao<JASCClassifyUnit, String> cuDao = DaoManager.createDao(connection, JASCClassifyUnit.class);
		Dao<ExtractionUnit, String> exuDao = DaoManager.createDao(connection, ExtractionUnit.class);
		if (!exuDao.isTableExists())
			TableUtils.createTable(exuDao);

		Dao<ExtractedEntity, String> ieDao = DaoManager.createDao(connection, ExtractedEntity.class);
		if (!ieDao.isTableExists())
			TableUtils.createTable(ieDao);

		QueryBuilder<JASCClassifyUnit, String> cuQueryBuilder = cuDao.queryBuilder().orderBy("id", true);
		QueryBuilder<ExtractionUnit, String> exuQueryBuilder = exuDao.queryBuilder();

		List<JASCClassifyUnit> classifyUnits = null;
		List<ExtractionUnit> extractionUnits = null;
		List<ExtractionUnit> newInitEUs = null;
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> extractions = null;
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> allExtractions = new HashMap<>();

		PreparedQuery<JASCClassifyUnit> cuPrepQuery;
		PreparedQuery<ExtractionUnit> exuPrepQuery;
		
		RatePatternExtraction rater = new RatePatternExtraction(jobs.entities, jobs.negExamples);

		Where<JASCClassifyUnit, String> whereClause;
		switch (type) {
		case COMPETENCE_IN_2:
			whereClause = cuQueryBuilder.where().eq("ClassTWO", true);
			break;
		case COMPETENCE_IN_3:
			whereClause = cuQueryBuilder.where().eq("ClassTHREE", true);
			break;
		case COMPETENCE_IN_23:
			whereClause = cuQueryBuilder.where().eq("ClassTWO", true).or().eq("ClassTHREE", true);
			break;
		case TOOL:
			whereClause = cuQueryBuilder.where().eq("ClassTWO", true).or().eq("ClassTHREE", true);
			break;
		default:
			whereClause = cuQueryBuilder.where().eq("ClassTWO", true).or().eq("ClassTHREE", true);
			break;
		}

		/**
		 * 
		 * TODO (JB)(!!) queryLimit, offset etc. auf classifyUnits oder jobAds bezogen??
		 * JB: aktuell beziehen sich beide Werte auf die Anzahl der zu verarbeitenden
		 * ClassifyUnits. Falls es auf jobads bezogen werden soll, muss hier ein Select auf
		 * jobads mit offset&limit und ein join über classifyUnit.jobadid durchgeführt werden
		 */

		if (queryLimit < 0) {
			QueryBuilder<JASCClassifyUnit, String> queryBuilder = cuDao.queryBuilder();
			queryBuilder.setWhere(whereClause);
			queryLimit = (int) queryBuilder.countOf();
			log.info("queryLimit nicht gesetzt. Neues queryLimit: " + queryLimit);
		}
		Instant startTime = Instant.now();
		Instant nowTime;
		int cuCount = 0;
		while (cuCount < queryLimit) {

			// pruefen, ob fetchsize uber querylimit hinaus geht
			if ((cuCount + fetchSize) > queryLimit)
				fetchSize = queryLimit - cuCount;

			cuQueryBuilder = cuQueryBuilder.offset((long) queryOffset).limit((long) fetchSize);
			cuQueryBuilder.setWhere(whereClause);
			cuPrepQuery = cuQueryBuilder.prepare();

			// alle relevanten Paragraphen, die durchsucht werden sollen
			classifyUnits = cuDao.query(cuPrepQuery);

			// falls Anfrage auf keine classify units zutrifft, wird abgebrochen
			if (classifyUnits.size() < 1) {
				log.info("no further relevant classify units found.");
				return;
			}

			for (JASCClassifyUnit cu : classifyUnits)
				cuDao.refresh(cu);
			cuCount += classifyUnits.size();

			// extraction units aus bereits initalisierten classifyUnits anfragen
			// Annahme: wenn es bereits extactionUnits mit paragraph_id = classifyUnit-id
			// gibt, dann wurde der Paragraph schon vorverarbeitet
			List<Integer> cuIds = classifyUnits.stream().map(JASCClassifyUnit::getId).collect(Collectors.toList());
			exuPrepQuery = exuQueryBuilder.where().in("paragraph_id", cuIds).prepare();
			extractionUnits = exuDao.query(exuPrepQuery);

			for (ExtractionUnit eu : extractionUnits)
				exuDao.refresh(eu);

			// bereits vorverarbeitete classifyUnits werden aus allen angefragten gelöscht
			Set<JASCClassifyUnit> iniParas = new HashSet<>(
					extractionUnits.stream().map(ExtractionUnit::getParagraph).collect(Collectors.toList()));
			// TODO JB: die Abfrage/Filterung von bereits verarbeiteten Paragraphen klappt noch nicht richtig
			classifyUnits.removeAll(iniParas);

			nowTime = Instant.now();
			String elapsed = Duration.between(startTime, nowTime).toString();
			double percentDone = (double) (cuCount - classifyUnits.size()) / queryLimit;
			log.info("processing row {} to {}, {} classify units remaining " +
							"({}% done, elapsed time: {})",
					queryOffset, (queryOffset + fetchSize), (queryLimit-cuCount),
					String.format("%,.3f", percentDone), elapsed);
			// TODO JB: für CUs prüfen, ob NLP-Daten schon vorliegen
			// Paragraphen in Sätze splitten und in ExtractionUnits überführen
			newInitEUs = ExtractionUnitBuilder.initializeIEUnits(classifyUnits, lemmatizer, null, tagger);

			for (ExtractionUnit eu : newInitEUs) {
				try {
					exuDao.create(eu);
				} catch (SQLException e) {
					//log.error(e.getMessage());
					
				}
			}

			extractionUnits.addAll(newInitEUs);
			log.info(extractionUnits.size() + " extraction Units (Sätze) initialisiert");

			// Informationsextraktion
			// TODO annotation für alle EUs oder nur neu initalisierte?
			jobs.annotateTokens(extractionUnits);
			extractions = jobs.extractEntities(extractionUnits, lemmatizer);

			possCompoundSplits.putAll(jobs.getNewCompounds());
			
			// TODO Was ist mit doppelten Extraktion (gleiche IE aus unterschiedlichen
			// Abschnitten)?

			// Entfernen der bereits bekannten Entitäten
			extractions = removeKnownEntities(extractions);
			
			//Aufruf der Confidenceberechnung und Selektion
			rater.evaluatePattern(extractions);
			rater.evaluateSeed(extractions);
			extractions = rater.selectBestEntities(extractions);
			
			for (Map.Entry<ExtractionUnit, Map<InformationEntity, List<Pattern>>> e : extractions.entrySet()) {
				for (Map.Entry<InformationEntity, List<Pattern>> ie : e.getValue().entrySet()) {
					try {
						((ExtractedEntity) ie.getKey()).setPatternString(ie.getValue());
						ieDao.create(((ExtractedEntity) ie.getKey()));
					} catch (SQLException e1) {
						log.debug(e1.getMessage());
					}
				}

			}
			allExtractions.putAll(extractions);
			
			queryOffset += fetchSize;

		}
		//rater.countPatternExtraction(allExtractions);

	}

	public void stringMatch(int queryOffset, int queryLimit, int fetchSize) throws SQLException, IOException {
		Tool lemmatizer = new Lemmatizer(PropertiesHandler.getLemmatizerModel(), false);

		Dao<JASCClassifyUnit, String> cuDao = DaoManager.createDao(connection, JASCClassifyUnit.class);
		Dao<ExtractionUnit, String> exuDao = DaoManager.createDao(connection, ExtractionUnit.class);
		if (!exuDao.isTableExists())
			TableUtils.createTable(exuDao);

		Dao<MatchedEntity, String> ieDao = DaoManager.createDao(connection, MatchedEntity.class);
		if (!ieDao.isTableExists())
			TableUtils.createTable(ieDao);

		// ORDER BY muss vor der Schleife gesetzt werden, sonst wird bei jeder Runde ein "id" im Ausdruck ergänzt
		QueryBuilder<JASCClassifyUnit, String> cuQueryBuilder = cuDao.queryBuilder().orderBy("id", true);
		QueryBuilder<ExtractionUnit, String> exuQueryBuilder = exuDao.queryBuilder();

		List<JASCClassifyUnit> classifyUnits = null;
		List<ExtractionUnit> extractionUnits = null;
		List<ExtractionUnit> newInitEUs = null;
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> stringMatches = null;
		PreparedQuery<JASCClassifyUnit> cuPrepQuery;
		PreparedQuery<ExtractionUnit> exuPrepQuery;

		// TODO in Methode auslagern
		Where<JASCClassifyUnit, String> whereClause;
		switch (type) {
		case COMPETENCE_IN_2:
			whereClause = cuQueryBuilder.where().eq("ClassTWO", true);
			break;
		case COMPETENCE_IN_3:
			whereClause = cuQueryBuilder.where().eq("ClassTHREE", true);
			break;
		case COMPETENCE_IN_23:
			whereClause = cuQueryBuilder.where().eq("ClassTWO", true).or().eq("ClassTHREE", true);
			break;
		case TOOL:
			whereClause = cuQueryBuilder.where().eq("ClassTWO", true).or().eq("ClassTHREE", true);
			break;
		default:
			whereClause = cuQueryBuilder.where().eq("ClassTWO", true).or().eq("ClassTHREE", true);
			break;
		}

		if (queryLimit < 0) {
			QueryBuilder<JASCClassifyUnit, String> queryBuilder = cuDao.queryBuilder();
			queryBuilder.setWhere(whereClause);
			queryLimit = (int) queryBuilder.countOf();
			// TODO queryOffset abziehen prüfen
			queryLimit = queryLimit - queryOffset;
			log.info("queryLimit nicht gesetzt. Neues queryLimit: " + queryLimit);
		}

		int cuCount = 0;
		Instant startTime = Instant.now();
		Instant nowTime;
		while (cuCount < queryLimit) {

			// pruefen, ob fetchsize uber querylimit hinaus geht
			if ((cuCount + fetchSize) > queryLimit)
				fetchSize = queryLimit - cuCount;

			cuQueryBuilder = cuQueryBuilder.offset((long) queryOffset)
					.limit((long) fetchSize);
			cuQueryBuilder.setWhere(whereClause);
			cuPrepQuery = cuQueryBuilder.prepare();

			// alle relevanten Paragraphen, die durchsucht werden sollen
			classifyUnits = cuDao.query(cuPrepQuery);

			// falls Anfrage auf keine classify units zutrifft, wird abgebrochen
			if (classifyUnits.size() < 1) {
				log.info("no further relevant classify units found.");
				return;
			}

			for (JASCClassifyUnit cu : classifyUnits)
				cuDao.refresh(cu);
			cuCount += classifyUnits.size();

			// extraction units aus bereits initalisierten classifyUnits anfragen
			// Annahme: wenn es bereits extactionUnits mit paragraph_id = classifyUnit-id
			// gibt, dann wurde der Paragraph schon vorverarbeitet
			List<Integer> cuIds = classifyUnits.stream().map(JASCClassifyUnit::getId).collect(Collectors.toList());
			exuPrepQuery = exuQueryBuilder.where().in("paragraph_id", cuIds).prepare();
			extractionUnits = exuDao.query(exuPrepQuery);

			for (ExtractionUnit eu : extractionUnits)
				exuDao.refresh(eu);

			nowTime = Instant.now();
			String elapsed = Duration.between(startTime, nowTime).toString();
			double percentDone = (double) (cuCount - classifyUnits.size()) / queryLimit;
			log.info("processing row {} to {}, {} classify units remaining " +
							"({}% done, elapsed time: {})",
					queryOffset, (queryOffset + fetchSize), (queryLimit-cuCount),
					String.format("%,.3f", percentDone), elapsed);

			// Paragraphen in Sätze splitten und in ExtractionUnits überführen
			newInitEUs = ExtractionUnitBuilder.initializeIEUnits(classifyUnits, lemmatizer, null, null);

			for (ExtractionUnit eu : newInitEUs) {
				try {
					exuDao.create(eu);
				} catch (SQLException e) {
				}
			}

			extractionUnits.addAll(newInitEUs);
			log.info(extractionUnits.size() + " extraction Units (Sätze) initialisiert");

			// Matching
			jobs.annotateTokens(extractionUnits);

			stringMatches = matchBatch(extractionUnits, /*stringMatches,*/ lemmatizer);

			// persist matches
			for (Map.Entry<ExtractionUnit, Map<InformationEntity, List<Pattern>>> e : stringMatches.entrySet()) {
				for (Map.Entry<InformationEntity, List<Pattern>> ie : e.getValue().entrySet()) {
					try {
						ieDao.create(((MatchedEntity) ie.getKey()));
					} catch (SQLException e1) {
						log.debug(e1.getMessage());
					}
				}

			}
			queryOffset += fetchSize;
		}

	}
	
	private Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> matchBatch(List<ExtractionUnit> extractionUnits,
			/*Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> stringMatches,*/ Tool lemmatizer)
			throws SQLException, IOException {

		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> stringMatches = jobs.extractByStringMatch(extractionUnits, lemmatizer);
		stringMatches = jobs.mergeInformationEntities(stringMatches);
		
		

		// set Modifiers
//		if (jobs.type == IEType.COMPETENCE_IN_3) {
		if (jobs.type != IEType.TOOL) {
			jobs.setModifiers(stringMatches);
		}

		return stringMatches;
	}

	private Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> removeKnownEntities(
			Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> extractions) {
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> toReturn = new HashMap<>();
		for (ExtractionUnit extractionUnit : extractions.keySet()) {

			Map<InformationEntity, List<Pattern>> ies = extractions.get(extractionUnit);
			Map<InformationEntity, List<Pattern>> filterdIes = new HashMap<>();
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
