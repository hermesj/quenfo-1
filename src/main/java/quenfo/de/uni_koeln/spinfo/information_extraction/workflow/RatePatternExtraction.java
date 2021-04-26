package quenfo.de.uni_koeln.spinfo.information_extraction.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import quenfo.de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.InformationEntity;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.Pattern;

/**
 * Evaluation of the used pattern in one iteration and the extracted
 * competences.
 * 
 * @author Christine Schaefer
 *
 */
public class RatePatternExtraction {

	/**
	 * Computes the confidence of the used patterns: Conf(P) = P.pos / (P.pos +
	 * P.neg)
	 * 
	 * @param knownEntites
	 * @param extractions
	 * 
	 * @return pattern-confidence
	 * 
	 */
	@SuppressWarnings("unlikely-arg-type")
	public void evaluatePattern(Set<String> competences, Set<String> noCompetences,
			Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> extractions) {

		List<Pattern> usedPattern = new ArrayList<Pattern>();

		List<String> validatedCompetences = new ArrayList<String>();
		List<String> knownExtractionFails = new ArrayList<String>();

		for (ExtractionUnit extractionUnit : extractions.keySet()) {
			Map<InformationEntity, List<Pattern>> ieP = extractions.get(extractionUnit);

			for (InformationEntity ie : ieP.keySet()) {
				List<Pattern> iePattern = ieP.get(ie);
				for (Pattern pattern : iePattern) {
					if (!usedPattern.contains(pattern))
						usedPattern.add(pattern);
				}
			}
		}

		for (String s : competences) {
			if (!(validatedCompetences.contains(s))) {
				validatedCompetences.add(s);
			}
		}

		for (String s : noCompetences) {
			if (!(knownExtractionFails.contains(s))) {
				knownExtractionFails.add(s);
			}
		}

		// Iteration über jedes genutzte Muster, das in der Extraktionsmap aufgelistet
		// ist
		for (Pattern p : usedPattern) {

			int tp = 0;
			int fp = 0;

			// Liste mit den Extraktionen des aktuellen Musters
			List<InformationEntity> extractionsOfPattern = new ArrayList<InformationEntity>();

			for (ExtractionUnit extractionUnit : extractions.keySet()) {
				Map<InformationEntity, List<Pattern>> ieP = extractions.get(extractionUnit);
				for (InformationEntity ie : ieP.keySet()) {
					if (ieP.get(ie).contains(p)) {
						extractionsOfPattern.add(ie);
					}
				}
			}

			// Vergleich der Extraktionen mit den validierten Kompetenzen
			for (InformationEntity ie : extractionsOfPattern) {
				if (validatedCompetences.contains(ie)) {
					tp++;
				}
				if (knownExtractionFails.contains(ie.getLemmaExpression())) {
					fp++;
				}
			}

			// Hinzufügen des Musters mit ermittelten Confidence-Wert
			// eigentlich ist hier die Map nicht mehr notwendig, da direkt der Conf-Wert des
			// Patterns mit setConf gesetzt wird
			p.setConf(tp, fp);

			// TP und FP für neues Pattern wieder null setzen
			tp = 0;
			fp = 0;

		}
	}

	@SuppressWarnings("unlikely-arg-type")
	public void evaluatePattern(Map<String, Set<InformationEntity>> competences,
			Map<String, Set<String[]>> noCompetences,
			Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> extractions) {

		List<Pattern> usedPattern = new ArrayList<Pattern>();

		List<String> validatedCompetences = new ArrayList<String>();
		List<String> knownExtractionFails = new ArrayList<String>();

		for (ExtractionUnit extractionUnit : extractions.keySet()) {
			Map<InformationEntity, List<Pattern>> ieP = extractions.get(extractionUnit);

			for (InformationEntity ie : ieP.keySet()) {
				List<Pattern> iePattern = ieP.get(ie);
				for (Pattern pattern : iePattern) {
					if (!usedPattern.contains(pattern))
						usedPattern.add(pattern);
				}
			}
		}
		// System.out.println("Genutzte Muster: " + usedPattern.size());

		for (String s : competences.keySet()) {
			Set<InformationEntity> ies = competences.get(s);

			for (InformationEntity ie : ies) {
				if (!(validatedCompetences.contains(ie.getLemmaExpression()))) {
					validatedCompetences.add(ie.getLemmaExpression());
				}
			}

		}
		// System.out.println("Bekannte Entitäten: " + validatedCompetences.size());

		for (String s : noCompetences.keySet()) {
			Set<String[]> noComp = noCompetences.get(s);
			for (String[] t : noComp) {
				for (int i = 0; i < t.length; i++) {
					if (!(knownExtractionFails.contains(t[i]))) {
						knownExtractionFails.add(t[i]);
					}
				}

			}
		}
		// System.out.println("Bekannte Extraktionsfehler: " +
		// knownExtractionFails.size());

		// Iteration über jedes genutzte Muster, das in der Extraktionsmap aufgelistet
		// ist
		for (Pattern p : usedPattern) {

			int tp = 0;
			int fp = 0;

			// Liste mit den Extraktionen des aktuellen Musters
			List<InformationEntity> extractionsOfPattern = new ArrayList<InformationEntity>();

			for (ExtractionUnit extractionUnit : extractions.keySet()) {
				Map<InformationEntity, List<Pattern>> ieP = extractions.get(extractionUnit);
				for (InformationEntity ie : ieP.keySet()) {
					if (ieP.get(ie).contains(p)) {
						extractionsOfPattern.add(ie);
					}
				}
			}

			// Vergleich der Extraktionen mit den validierten Kompetenzen
			for (InformationEntity ie : extractionsOfPattern) {
				if (validatedCompetences.contains(ie)) {
					tp++;
				}
				if (knownExtractionFails.contains(ie.getLemmaExpression())) {
					fp++;
				}
			}
			// System.out.println("Extraktionen eines Musters: " +
			// extractionsOfPattern.size());

			// Hinzufügen des Musters mit ermittelten Confidence-Wert
			p.setConf(tp, fp);
			// System.out.println(p.getConf());

			// TP und FP für neues Pattern wieder null setzen
			tp = 0;
			fp = 0;

		}
	}

	/**
	 * Computes the confidence of the extraction: 1 - (Produkt(1 - Conf(p)))
	 * 
	 * @param allextractions
	 * 
	 * @return extraction confidence
	 */
	public void evaluateSeed(Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> allextractions) {
		// Conf(seed) = 1 - <Produkt>(1-Conf(P))

		for (ExtractionUnit extractionUnit : allextractions.keySet()) {
			Map<InformationEntity, List<Pattern>> extraction = allextractions.get(extractionUnit);

			for (InformationEntity ie : extraction.keySet()) {
				List<Pattern> pattern = extraction.get(ie);

				List<Pattern> usedPattern = new ArrayList<Pattern>();
				for (Pattern p : pattern) {
					if (!usedPattern.contains(p))
						usedPattern.add(p);
				}

				//System.out.println("Genutzte Muster: " + usedPattern.size());
				ie.setConf(usedPattern);
			}
		}
	}

	/**
	 * Select extractions with confidence >= 0.6
	 * 
	 * @param allExtractions
	 * @return selected extractions
	 */
	public Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> selectBestEntities(
			Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> allExtractions) {
		Map<ExtractionUnit, Map<InformationEntity, List<Pattern>>> toReturn = new HashMap<ExtractionUnit, Map<InformationEntity, List<Pattern>>>();
		for (ExtractionUnit extractionUnit : allExtractions.keySet()) {
			Map<InformationEntity, List<Pattern>> ies = allExtractions.get(extractionUnit);
			Map<InformationEntity, List<Pattern>> filterdIes = new HashMap<InformationEntity, List<Pattern>>();
			for (InformationEntity ie : ies.keySet()) {
				if (ie.getConf() == 0.0 || ie.getConf() >= 0.6) {
				//if(ie.getConf() >= 0.1) {
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
