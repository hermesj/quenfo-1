package quenfo.de.uni_koeln.spinfo.information_extraction.data;

import java.util.HashSet;
import java.util.Set;

import com.j256.ormlite.table.DatabaseTable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@DatabaseTable(tableName = "Matches")
@Data
@EqualsAndHashCode(of = {}, callSuper = true)
public class MatchedEntity extends InformationEntity {

	// übergeordnetes Konzept der Kompetenz /des Tools
	@Setter(AccessLevel.NONE)
	private Set<String> labels;
	
	public MatchedEntity() {
		
	}

	public MatchedEntity(String startLemma, boolean isSingleWordEntity, IEType type) {
		super(startLemma, isSingleWordEntity, type);
	}

	public MatchedEntity(String startLemma, boolean isSingleWordEntity, int firstIndex, IEType type,
			ExtractionUnit parent) {
		super(startLemma, isSingleWordEntity, firstIndex, type, parent);
	}
	
	
	/**
	 * constructor for known information entities (without extractionUnit)
	 * @param startLemma
	 * @param isSingleWordEntity
	 * @param type
	 * @param label
	 */
	public MatchedEntity(String startLemma, boolean isSingleWordEntity,
			IEType type, String label) {
		this(startLemma, isSingleWordEntity, -1, type, null);
		this.labels = new HashSet<>();
		labels.add(label);
	}
	
	/**
	 * constructor for known information entities (without extractionUnit)
	 * @param startLemma
	 * @param isSingleWordEntity
	 * @param type
	 * @param labels
	 */
	public MatchedEntity(String startLemma, boolean isSingleWordEntity,
			IEType type, Set<String> labels) {
		this(startLemma, isSingleWordEntity, -1, type, null);
		this.labels = labels;
	}
	
	public MatchedEntity(String startLemma, boolean isSingleWordEntity, IEType type, Set<String> labels,
			ExtractionUnit parent) {
		super(startLemma, isSingleWordEntity, type, parent);
		this.labels = labels;
	}

	public void addLabel(String label) {
		labels.add(label);
	}
	

}
