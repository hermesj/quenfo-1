package quenfo.de.uni_koeln.spinfo.classification.core.data;

import java.util.List;
import java.util.UUID;

import com.j256.ormlite.table.DatabaseTable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author geduldia
 * 
 *         represents a basic classification-object
 *
 */
@DatabaseTable(tableName = "classifyunits")
@Data
@EqualsAndHashCode(of = { "content", "jahrgang", "postingID" })
@ToString(of = { "content" }) //TODO JB: toString classifyUnit
public class ClassifyUnit {
	
	@Deprecated
	protected UUID id;
	
	/**
	 * publication year of jobad posting that contains this classify unit
	 */
	@Setter(AccessLevel.NONE)
	private int jahrgang;
	
	/**
	 * ID of jobad posting that contains this classify unit
	 */
	@Setter(AccessLevel.NONE)
	private String postingID = "";
	
	private int tableID = -1;

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
	private String sentences;
	
	private String lemmata;
	private String posTags;
	private String tokens;

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
	public ClassifyUnit(String content, UUID id, int jahrgang) {
		this.id = id;
		this.content = content;
		this.jahrgang = jahrgang;
	}

	/**
	 * @param content
	 * @param id
	 * @param postingID 
	 */
	public ClassifyUnit(String content, UUID id, int jahrgang, String postingID) {
		this.id = id;
		this.content = content;
		this.postingID = postingID;
		this.jahrgang = jahrgang;
	}



}
