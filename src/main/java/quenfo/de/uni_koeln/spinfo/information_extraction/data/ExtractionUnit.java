package quenfo.de.uni_koeln.spinfo.information_extraction.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import is2.data.SentenceData09;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;

/**
 * @author geduldia
 * 
 *         A part of a job-ad smaller than a paragraph. (usually a sentence)
 *         Includes a lot of lexial data (lemmata, posTags,...) which is needed
 *         for the information extraction.
 *
 */

//@NamedQuery(
//		name = "getClassXExtractionUnits", 
//		query = "SELECT e FROM ExtractionUnit e JOIN JASCClassifyUnit  c ON e.classifyUnitjpaID = c.jpaID WHERE c.actualClassID = :class"		
//		)
//
//@Entity
@Data
@EqualsAndHashCode(of = { "postingID", "classifyUnitID", "sentence" })
@ToString(of = { "sentence" })
public class ExtractionUnit implements Serializable {
	

	private static final long serialVersionUID = 1L;
	
	/**
	 * first ID of the containing JobAd (Jahrgang)
	 */
	private String jahrgang;
	/**
	 * second ID of the containing JobAd (former: Zeilennummer)
	 */
	private String postingID;
	
	/**
	 * ID of the containing classifyUnit (paragraph)
	 */
	@Deprecated
	private UUID classifyUnitID;
	
	private ClassifyUnit paragraph;

	/**
	 * ID of the containing classifyUnit in SQLite table
	 */
	private int classifyUnitTableID;

	/**
	 * ID of this extractionUnit
	 */
	private UUID sentenceID;

	/**
	 * content of this extractionUnit
	 */
	private String sentence;
	
	/**
	 * Tokens in this sentence
	 */
	private List<TextToken> tokenObjects = new ArrayList<TextToken>();


	@Getter(AccessLevel.NONE)
	private String[] tokens;

	@Getter(AccessLevel.NONE)
	private String[] lemmata;

	@Getter(AccessLevel.NONE)
	private String[] posTags;

	
	private boolean lexicalDataStoredInDB;

	


	public ExtractionUnit() {
		this.sentenceID = UUID.randomUUID();
	}

	/**
	 * @return tokens produced by the MateTool
	 */
	public String[] getTokens() {
		if (this.tokens != null)
			return tokens;
		return null;
	}

	/**
	 * @return lemmata produced by the MateTool
	 */
	public String[] getLemmata() {
		if (this.lemmata != null)
			return this.lemmata;
		return null;
	}

	/**
	 * @return posTags produced by the MateTool
	 */
	public String[] getPosTags() {
		if (this.posTags != null)
			return this.posTags;
		return null;
	}

//	/**
//	 * @return morphTags produced by the MateTool
//	 */
//	@Deprecated
//	public String[] getMorphTags() {
//		return sentenceData.pfeats;
//	}

	/**
	 * creates the List of Token-objects for this ExtractionUnit Sets a Root-Token
	 * as first Token and an End-Token as last Token
	 * 
	 * @param sentenceData
	 */
	public void setSentenceData(SentenceData09 sentenceData) {
		TextToken token = null;
		
		this.tokens = sentenceData.forms;
		this.lemmata = sentenceData.plemmas;
		this.posTags = sentenceData.ppos;
		
		for (int i = 0; i < tokens.length; i++) {
			if (posTags == null) {
				token = new TextToken(tokens[i], lemmata[i], null);
			} else {
				token = new TextToken(tokens[i], lemmata[i], posTags[i]);
			}
			this.tokenObjects.add(token);
		}
		token = new TextToken(null, "<end-LEMMA>", "<end-POS>");
		
		this.tokenObjects.add(token);
	}

	public void deleteData() {
		this.tokenObjects = null;
	}


}
