package quenfo.de.uni_koeln.spinfo.classification.core.data;

import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import quenfo.de.uni_koeln.spinfo.core.data.JobAd;

/**
 * 
 * @author geduldia
 * 
 *         represents a basic classification-object
 *
 */

@Data
@EqualsAndHashCode(of = { "jobad", "content" })
@ToString(of = { "id", "jobad" }) //TODO JB: toString classifyUnit
public class ClassifyUnit {
	
	@DatabaseField(generatedId = true, index = true, indexName = "cuindex")
	@Setter(AccessLevel.NONE)
	private Integer id;
	
	/**
	 * jobad posting that contains this classify unit
	 */
	@DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
	private JobAd jobad;
	
	private int tableID = -1;

	@DatabaseField(uniqueCombo = true, columnName = "TEXT")
	protected String content;


	/**
	 * list of features
	 */
	private List<String> featureUnits;

	/**
	 * weighted document vector
	 */
	private double[] featureVector;
	
	/**
	 * String mit Sätzen (durch || getrennt)
	 */
//	private String sentences;
	
	private String lemmata;
	private String posTags;
	private String tokens;
	
	/**
	 * publication year of jobad posting that contains this classify unit
	 */
	@Deprecated
	@Setter(AccessLevel.NONE)
	private String jahrgang;
	
	/**
	 * ID of jobad posting that contains this classify unit
	 */
	@Deprecated
	@Setter(AccessLevel.NONE)
	private String postingID = "";
	


	/**
	 * default constructor for object relational mapping
	 */
	public ClassifyUnit() {
		
	}
	
	
	/**
	 * @param content
	 * @param id
	 * @param postingID 
	 */
	public ClassifyUnit(String content, /*UUID id,*/ String jahrgang) {
		this.content = content;
		this.jahrgang = jahrgang;
	}

	/**
	 * @param content
	 * @param id
	 * @param postingID 
	 */
	public ClassifyUnit(String content, /*UUID id,*/ String jahrgang, String postingID) {
		this.content = content;
		this.postingID = postingID;
		this.jahrgang = jahrgang;
	}


	public ClassifyUnit(String content, JobAd jobad) {
		this.content = content;
		this.jobad = jobad;
	}
	
//	@Override
//	public String toString() {
//		if (this.content != null && content.length() > 10 && this.jobad != null)
//			return new StringJoiner("|", "[", "]")
//					.add("substring=" + content.subSequence(0, 10))
//					.add("jobad=" + jobad.toString())
//					.toString();
//		
//		if (this.jobad != null)
//			return new StringJoiner("|", "[", "[")
//			.add("jobad=" + jobad.toString())
//			.toString();
//		
//	}



}
