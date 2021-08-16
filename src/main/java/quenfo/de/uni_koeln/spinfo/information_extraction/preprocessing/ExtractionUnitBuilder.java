package quenfo.de.uni_koeln.spinfo.information_extraction.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import is2.tools.Tool;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;

/**
 * @author geduldia
 * 
 *         A class to separate ClassifyUnits (~paragraphs) into ExtractionUnits
 *         (~sentences)
 *
 */
public class ExtractionUnitBuilder {

	/**
	 * 
	 * transforms a list of classifyUnits (≈ paragraphs) in a list of
	 * extractionUnits (≈ sentences)
	 * 
	 * @param classifyUnits
	 * @param lemmatizer
	 * @param morphTagger
	 * @param tagger
	 * @return list of initialized extractionUnits
	 * @throws IOException
	 */
	public static List<ExtractionUnit> initializeIEUnits(List<? extends ClassifyUnit> classifyUnits, Tool lemmatizer,
			Tool morphTagger, Tool tagger) throws IOException {
		List<ExtractionUnit> extractionUnits = new ArrayList<ExtractionUnit>();
		IETokenizer tokenizer = new IETokenizer();
		List<String> sentences;
//		List<String> lemmata;
//		List<String> posTags;
//		List<String> tokens;
		ExtractionUnit extractionUnit = null;
		for (ClassifyUnit cu : classifyUnits) {
			
			sentences = null;
//			lemmata = null;
//			posTags = null;
//			tokens = null;
			
			// TODO check: an ORM angepasst
//			if (cu.getSentences() == null) {
//				sentences = tokenizer.splitIntoSentences(cu.getContent());
//			} else {
//				sentences = Arrays.asList(cu.getSentences().split("  \\|\\|  "));
//			}
			
			sentences = tokenizer.splitIntoSentences(cu.getContent());
			
			
//			if (cu.getLemmata() != null) {
//				lemmata = Arrays.asList(cu.getLemmata().split("  \\|\\|  "));
//			}
//			if (cu.getPosTags() != null) {
//				posTags = Arrays.asList(cu.getPosTags().split("  \\|\\|  "));
//			}
//			if (cu.getTokens() != null) {
//				tokens = Arrays.asList(cu.getTokens().split("  \\|\\|  "));
//			}
//			
			
			for (int i = 0; i < sentences.size(); i++) {
				String sentence = sentences.get(i);
				sentence = correctSentence(sentence);
				if (sentence.length() > 1) {
					extractionUnit = new ExtractionUnit(i);

					extractionUnit.setSentence(sentence);
//					extractionUnit.setJahrgang(cu.getJahrgang());
//					extractionUnit.setPostingID(cu.getPostingID());
					extractionUnit.setParagraph((JASCClassifyUnit) cu);
//					extractionUnit.setClassifyUnitTableID(cu.getTableID());
				
//					if (lemmata != null) {
//						extractionUnit.setLemmata(lemmata.get(i).split(" \\| "));
//					}
//					if (posTags != null) {
//
//						extractionUnit.setPosTags(posTags.get(i).split(" \\| "));
//					}
//					if (tokens != null) {
//						extractionUnit.setTokens(tokens.get(i).split(" \\| "));
//					}
					extractionUnits.add(extractionUnit);
				}
			}
		}
		MateTagger.setLexicalData(extractionUnits, lemmatizer, morphTagger, tagger);
		classifyUnits = null;
		return extractionUnits;
	}

	private static String correctSentence(String sentence) {
		/* normalisiert Leerzeichen vor und nach Punkt und Komma. Wenn vor einem Punkt oder Komma ein
		* Leerzeichen steht, aber dahinter keins, wird hinter Punkt bzw. Komma zusätzlich ein Leerzeichen eingefügt.
		* Beispiel: */
		String regex = "\\s([\\,\\.])(\\w..)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sentence);
		while (m.find()) {
			if (!m.group(2).toLowerCase().equals("net")) { // Außnahme: .NET (Microsoft-Framework)
				sentence = sentence.replace(m.group(1), m.group(1) + " ");
			}
		}
		/* Falls zwischen zwei Wörtern ein Komma bzw. Semikolon ohne Leerzeichen steht, wird nach Komma bzw. Semikolon
		* ein Leerzeichen eingefügt
		* Beispiel: Java,Python -> Java, Python */
		regex = "[A-Za-z](\\,|\\;)[A-Za-z]";
		p = Pattern.compile(regex);
		m = p.matcher(sentence);
		while (m.find()) {
			sentence = sentence.replace(m.group(), m.group().substring(0, 2) + " " + m.group().substring(2));
		}
		/*  TODO Regex entziffern */
		regex = "(\\s[\\*\\/])(\\w\\w)";
		p = Pattern.compile(regex);
		m = p.matcher(sentence);
		while (m.find()) {
			if (!m.group(2).toLowerCase().equals("in")) {
				sentence = sentence.replace(m.group(1), " ");
			} else {
				sentence = sentence.replace(m.group(1), "/");
			}
		}
		// if (sentence.contains(" & ")) {
		// sentence = sentence.replace(" & ", " und ");
		// }
		/* 'und'/'oder' mit Großbuchstaben werden mit Kleinbuchstaben normalisiert */
		if (sentence.contains(" UND ")) {
			sentence = sentence.replace(" UND ", " und ");
		}
		if (sentence.contains(" ODER ")) {
			sentence = sentence.replace(" ODER ", " oder ");
		}
		/*  sämtliche Varianten von 'und/oder' 'und oder' 'und|oder' werden durch 'oder' ersetzt */
		regex = " und[-|\\/| ][\\/| ]?[ ]?oder ";
		p = Pattern.compile(regex);
		m = p.matcher(sentence);
		while (m.find()) {
			sentence = sentence.replace(m.group(), " oder ");
		}
		/* und das gleiche noch mal für 'oder/und' ... */
		regex = " oder[-|\\/| ][\\/| ]?[ ]?und ";
		p = Pattern.compile(regex);
		m = p.matcher(sentence);
		while (m.find()) {
			sentence = sentence.replace(m.group(), " und ");
		}

		return sentence;
	}



}
