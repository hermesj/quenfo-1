package quenfo.de.uni_koeln.spinfo.classification.core.data;

import java.util.List;
import java.util.UUID;

import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(of = { "id" })
@ToString(of = { "id" })
public class ClassifyUnit {

	protected String content;

	protected UUID id;

	// Derby ID der beihaltenden Stellenanzeige
	private Long jobAdJpaID;

	/**
	 * list of features
	 */
	private List<String> featureUnits;

	/**
	 * weighted document vector
	 */
	private double[] featureVector;

	/**
	 * default constructor for eclipseLink
	 */
	public ClassifyUnit() {
		this("", UUID.randomUUID());
	}

	/**
	 * @param content
	 * @param id
	 */
	public ClassifyUnit(String content, UUID id) {
		this.id = id;
		this.content = content;
	}

	/**
	 * 
	 * @param content
	 */
	public ClassifyUnit(String content) {
		this(content, UUID.randomUUID());
	}

}
