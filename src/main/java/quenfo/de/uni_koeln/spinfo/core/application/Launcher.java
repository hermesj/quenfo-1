package quenfo.de.uni_koeln.spinfo.core.application;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import org.apache.log4j.Logger;

import quenfo.de.uni_koeln.spinfo.classification.applications.ClassifyDatabase;
import quenfo.de.uni_koeln.spinfo.classification.applications.ClassifyORMLite;
import quenfo.de.uni_koeln.spinfo.information_extraction.applications.ExtractNewCompetences;
import quenfo.de.uni_koeln.spinfo.information_extraction.applications.ExtractNewTools;
import quenfo.de.uni_koeln.spinfo.information_extraction.applications.MatchCompetences;
import quenfo.de.uni_koeln.spinfo.information_extraction.applications.MatchTools;

/**
 * Class contains an application to run different workflows from command line
 * workflows: classify, extractCompetences, extractTools, matchCompetences, matchTools
 * @author Johanna Binnewitt
 *
 */
public class Launcher {
	
//	static Logger log = Logger.getLogger(Launcher.class);
	private static Logger log = LogManager.getLogger();
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		
		switch (args[0].toLowerCase()) {
		case "classify":
			log.info("--- Classification starts ---");
			ClassifyDatabase.main(args);
			break;
		case "extractcompetences":
			log.info("--- Extracting competences starts ---");
			ExtractNewCompetences.main(args);
			break;
		case "extracttools":
			log.info("--- Extracting tools starts ---");
			ExtractNewTools.main(args);
			break;
		case "matchcompetences":
			log.info("--- Matching competences starts ---");
			MatchCompetences.main(args);
			break;
		case "matchtools":
			log.info("--- Matching tools starts ---");
			MatchTools.main(args);
			break;
		//---------- ORM -------------------
		case "classifyorm":
			log.info("--- Classification with ORM starts ---");
			ClassifyORMLite.main(args);
			break;
		case "extractcompetencesorm":
			log.info("--- Extracting competences (with orm mapping) starts");
			quenfo.de.uni_koeln.spinfo.information_extraction.apps_orm.ExtractNewCompetences.main(args);
			break;
		case "extracttoolsorm":
			log.info("--- Extracting tools (with orm mapping) starts");
			quenfo.de.uni_koeln.spinfo.information_extraction.apps_orm.ExtractNewTools.main(args);
			break;
		case "matchcompetencesorm":
			log.info("--- Matching competences (with orm mapping) starts");
			quenfo.de.uni_koeln.spinfo.information_extraction.apps_orm.MatchCompetences.main(args);
			break;
		case "matchtoolsorm":
			log.info("--- not implemented yet");
			break;
		default:
			System.out.println(args[0] + " is not available. Please choose\n"
					+ "classify\n"
					+ "classifyORM\n"
					+ "extractCompetences\n"
					+ "extractTools\n"
					+ "matchCompetences\n"
					+ "matchTools\n"
					+ "extractCompetencesORM\n"
					+ "extractToolsORM");
			break;
		}
		

	}

}
