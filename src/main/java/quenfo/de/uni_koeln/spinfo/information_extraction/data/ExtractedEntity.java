package quenfo.de.uni_koeln.spinfo.information_extraction.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author geduldia
 * 
 *         represents a single information instance (a tool or a
 *         competence) defined by an expression of one or more lemmata.
 *
 */
@DatabaseTable(tableName = "Extractions")
@Data
@EqualsAndHashCode(of = {}, callSuper=true)
public class ExtractedEntity extends InformationEntity {
	
	
	@DatabaseField
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private String patternsString;
	
	
	
	private static StringJoiner sj;

	
	public ExtractedEntity() {
		
	}
	
	/**
	 * constructor for known information entities (without extractionUnit)
	 * @param startLemma
	 * @param isSingleWordEntity
	 * @param type
	 */
	public ExtractedEntity(String startLemma, boolean isSingleWordEntity, IEType type) {
		super(startLemma, isSingleWordEntity, type);
	}
	
	public ExtractedEntity(String startLemma, boolean isSingleWordEntity, 
			int firstIndex, IEType type, ExtractionUnit parent) {
		super(startLemma, isSingleWordEntity, firstIndex, type, parent);
	}
	

	/**
	 * @param startLemma 
	 * 			first token of this IE
	 * @param isSingleWordEntity
	 */
	public ExtractedEntity(String startLemma, boolean isSingleWordEntity,
			IEType type, ExtractionUnit parent) {
		super(startLemma, isSingleWordEntity, -1, type, parent);
	}
	

	


	
	



	public void setPatternString(List<Pattern> patterns) {
		ExtractedEntity.sj = new StringJoiner("|", "[", "]");
		for (Pattern p : patterns)
			sj.add(p.getDescription());
		this.patternsString = sj.toString();
	}


}
