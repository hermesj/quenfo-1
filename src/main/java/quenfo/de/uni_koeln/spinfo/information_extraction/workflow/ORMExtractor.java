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
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import is2.lemmatizer.Lemmatizer;
import is2.tag.Tagger;
import is2.tools.Tool;
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

		Dao<InformationEntity, String> ieDao = DaoManager.createDao(connection, InformationEntity.class);
		if (!ieDao.isTableExists())
			TableUtils.createTable(ieDao);

		QueryBuilder<JASCClassifyUnit, String> cuQueryBuilder = cuDao.queryBuilder();
		QueryBuilder<ExtractionUnit, String> exuQueryBuilder = exuDao.queryBuilder();

		List<JASCClassifyUnit> classifyUnits = null;
		List<ExtractionUnit> extractionUnits = null;
		List<ExtractionUnit> newInitEUs = null;
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> extractions = null;
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> allExtractions = new HashMap<ExtractionUnit, Map<InformationEntity, List<Pattern>>>();

		PreparedQuery<JASCClassifyUnit> cuPrepQuery;
		PreparedQuery<ExtractionUnit> exuPrepQuery;

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
			log.info("queryLimit nicht gesetzt. Neues queryLimit: " + queryLimit);
		}

		int cuCount = 0;
		while (cuCount < queryLimit) {

			// pruefen, ob fetchsize uber querylimit hinaus geht
			if ((cuCount + fetchSize) > queryLimit)
				fetchSize = queryLimit - cuCount;
//			log.info("extracting from {} paragraphs, skipping first {} rows", fetchSize, queryOffset);

			cuQueryBuilder = cuQueryBuilder.offset((long) queryOffset).orderBy("id", true).limit((long) fetchSize);
			cuQueryBuilder.setWhere(whereClause);
			cuPrepQuery = cuQueryBuilder.prepare();

			// alle relevanten Paragraphen, die durchsucht werden sollen
			classifyUnits = cuDao.query(cuPrepQuery);
			
			// falls Anfrage auf keine classify units zutrifft, wird abgebrochen
			if (classifyUnits.size() < 1)
				return;
			
			
			
			for(JASCClassifyUnit cu : classifyUnits)
				cuDao.refresh(cu);
			log.info(classifyUnits.size() + " classifyUnits abgefragt");
			cuCount += classifyUnits.size();

			// extraction units aus bereits initalisierten classifyUnits anfragen
			// Annahme: wenn es bereits extactionUnits mit paragraph_id = classifyUnit-id
			// gibt, dann wurde der Paragraph schon vorverarbeitet
			List<Integer> cuIds = classifyUnits.stream().map(JASCClassifyUnit::getId).collect(Collectors.toList());
			exuPrepQuery = exuQueryBuilder.where().in("paragraph_id", cuIds).prepare();
			extractionUnits = exuDao.query(exuPrepQuery);
			
			for (ExtractionUnit eu : extractionUnits)
				exuDao.refresh(eu);
			
			
//			for(ExtractionUnit eu : extractionUnits)
//				log.info(eu.getParagraph());

			// bereits vorverarbeitete classifyUnits werden aus allen angefragten gelöscht
			Set<JASCClassifyUnit> iniParas = new HashSet<>(extractionUnits.stream().map(ExtractionUnit::getParagraph)
					.collect(Collectors.toList()));
//			log.info("to remove: ");
//			for(JASCClassifyUnit cu : iniParas) {
////				cuDao.refresh(cu);
//				log.info(cu.toString());
//			}
//				
//			log.info("to classify: " + classifyUnits);
			classifyUnits.removeAll(iniParas);
			
			log.info("processing row {} to {}, extracting from {} classify units", queryOffset, (queryOffset + fetchSize), classifyUnits.size());

			// Paragraphen in Sätze splitten und in ExtractionUnits überführen
			newInitEUs = ExtractionUnitBuilder.initializeIEUnits(classifyUnits, lemmatizer, null, tagger);

			for (ExtractionUnit eu : newInitEUs) {
				try {
					exuDao.create(eu);
				} catch (SQLException e) {
					log.error(e.getMessage());
				}
			}

			extractionUnits.addAll(newInitEUs);
			log.info(extractionUnits.size() + " extraction Units (Sätze) initialisiert");

			// Informationsextraktion
			// TODO annotation für alle EUs oder nur neu initalisierte?
			jobs.annotateTokens(extractionUnits);
			log.info("extract " + type.name().toLowerCase());
			extractions = jobs.extractEntities(extractionUnits, lemmatizer);

			possCompoundSplits.putAll(jobs.getNewCompounds());

			// Entfernen der bereits bekannten Entitäten
			extractions = removeKnownEntities(extractions);

			for (Map.Entry<ExtractionUnit, Map<InformationEntity, List<Pattern>>> e : extractions.entrySet()) {
				for (Map.Entry<InformationEntity, List<Pattern>> ie : e.getValue().entrySet()) {
					try {
						ieDao.create(ie.getKey());
					} catch (SQLException e1) {
						log.debug(e1.getMessage());
					}
				}

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
