package quenfo.de.uni_koeln.spinfo.information_extraction.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import is2.data.SentenceData09;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;

/**
 * @author geduldia
 * 
 *         A part of a job-ad smaller than a paragraph. (usually a sentence)
 *         Includes a lot of lexial data (lemmata, posTags,...) which is needed
 *         for the information extraction.
 *
 */


@DatabaseTable(tableName = "ExtractionUnits")
@Data
@EqualsAndHashCode(of = { "postingID", "paragraph", "sentence" })
@ToString(of = { "sentence" })
public class ExtractionUnit implements Serializable {
	

	private static final long serialVersionUID = 1L;
	
	@DatabaseField(generatedId = true)
	private Integer id;
	
	
	/**
	 * position of this sentence in the paragraph
	 */
	@DatabaseField(uniqueCombo = true)
	private int positionIndex;
	
	/**
	 * first ID of the containing JobAd (Jahrgang)
	 */
	private String jahrgang;
	/**
	 * second ID of the containing JobAd (former: Zeilennummer)
	 */
	private String postingID;
	
	// TODO datatype JASC oder ClassifyUnit?
	@DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
	private JASCClassifyUnit paragraph;

//	/**
//	 * ID of the containing classifyUnit in SQLite table
//	 */
//	@Deprecated
//	private int classifyUnitTableID;

	/**
	 * ID of this extractionUnit
	 */
	private UUID sentenceID;

	/**
	 * content of this extractionUnit
	 */
	@DatabaseField(uniqueCombo = true)
	private String sentence;
	
	/**
	 * Tokens in this sentence
	 */
//	@Deprecated
//	private List<TextToken> tokenArrayList = new ArrayList<TextToken>();
	
	/**
	 * Tokens in this sentence
	 */
	@DatabaseField(dataType=DataType.SERIALIZABLE)
	private TextToken[] tokenArray;


	@Getter(AccessLevel.NONE)
	private String[] tokens;

	@Getter(AccessLevel.NONE)
	private String[] lemmata;

	@Getter(AccessLevel.NONE)
	private String[] posTags;

	
//	private boolean lexicalDataStoredInDB;

	public ExtractionUnit(int positionIndex) {
		this();
		this.positionIndex = positionIndex;
		
	}


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
		
		tokenArray = new TextToken[tokens.length + 1];
		
		for (int i = 0; i < tokens.length; i++) {
			if (posTags == null) {
				token = new TextToken(tokens[i], lemmata[i], null);
			} else {
				token = new TextToken(tokens[i], lemmata[i], posTags[i]);
			}
//			this.tokenArrayList.add(token);
			this.tokenArray[i] = token;
		}
		token = new TextToken(null, "<end-LEMMA>", "<end-POS>");
		
//		this.tokenArrayList.add(token);
		this.tokenArray[tokenArray.length-1] = token;
		
		
		
	}

	public void deleteData() {
//		this.tokenArrayList = null;
		this.tokenArray = null;
	}


}
