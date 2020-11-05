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
@EqualsAndHashCode(of = {"startLemma", "singleWordEntity", "lemmaArray"})
public class ExtractedEntity {
	
	@DatabaseField(generatedId = true)
	private Integer id;
	
	@DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
	@Setter(AccessLevel.NONE)
	private ExtractionUnit parent;

	//erstes Wort des Kompetenz-/Tool-Ausdrucks
	@DatabaseField
	@Setter(AccessLevel.NONE)
	private String startLemma;
	
	//ist 'true' wenn der Ausdruck aus nur einem Wort (startLemma) besteht
	@DatabaseField
	@Setter(AccessLevel.NONE)
	private boolean singleWordEntity;
	
	//ist 'true', wenn der Ausdruck aus einer Morphemkoordination aufgelöst wurde
//	@Getter(AccessLevel.NONE)
//	@Setter(AccessLevel.NONE)
//	private boolean fromMorphemCoordination;
	
	 //der vollständige Ausdruck als String-List (lemmata.get(0) = startLemma)
	@Deprecated
	@Setter(AccessLevel.NONE)
	private List<String> lemmaArrayList;
	
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	@Setter(AccessLevel.NONE)
	private String[] lemmaArray;
	
	@DatabaseField(uniqueCombo = true)
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private String lemmaExpression;
	
	//Modifizierer (z.B. 'zwingend erforderlich')
	@DatabaseField
	private String modifier;
	
	@DatabaseField
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private String patternsString;
	
	//übergeordnetes Konzept der Kompetenz (des Tools)
	@Setter(AccessLevel.NONE)
	private Set<String> labels;
	
	//Index des ersten Lemmatas im Satz
	@Setter(AccessLevel.NONE)
	private int firstIndex;
	
	//expandierte Koordinationen im Ausdruck (für Evaluierung)
	@Setter(AccessLevel.NONE)
	private List<String> coordinations;
	
//	//Tokens des Ausdrucks
//	@Getter(AccessLevel.NONE)
//	@Setter(AccessLevel.NONE)
//	private List<TextToken> originalEntity;
	
	
	
	@DatabaseField
	@Setter(AccessLevel.NONE)
	private IEType type;
	
	private static StringJoiner sj;
//	private static StringBuilder sb;
	
	public ExtractedEntity() {
		
	}
	
	/**
	 * constructor for known information entities (without extractionUnit)
	 * @param startLemma
	 * @param isSingleWordEntity
	 * @param type
	 */
	public ExtractedEntity(String startLemma, boolean isSingleWordEntity, IEType type) {
		this.startLemma = startLemma;
		this.singleWordEntity = isSingleWordEntity;
		this.type = type;
		if(isSingleWordEntity){			
			this.lemmaArray = new String[1];
			this.lemmaArray[0] = startLemma;
			this.lemmaExpression = startLemma;
		}

	}
	
	/**
	 * constructor for known information entities (without extractionUnit)
	 * @param startLemma
	 * @param isSingleWordEntity
	 * @param type
	 * @param label
	 */
	public ExtractedEntity(String startLemma, boolean isSingleWordEntity,
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
	public ExtractedEntity(String startLemma, boolean isSingleWordEntity,
			IEType type, Set<String> labels) {
		this(startLemma, isSingleWordEntity, -1, type, null);
		this.labels = labels;
	}
	
	/**
	 * @param startLemma 
	 * 			first token of this IE
	 * @param isSingleWordEntity
	 */
	public ExtractedEntity(String startLemma, boolean isSingleWordEntity, IEType type, ExtractionUnit parent) {
		this(startLemma, isSingleWordEntity, -1, type, parent);
	}
	

	
	public ExtractedEntity(String startLemma, boolean isSingleWordEntity, 
			int firstIndex, IEType type, ExtractionUnit parent) {
//		this.startLemma = startLemma;
//		this.singleWordEntity = isSingleWordEntity;
//		this.type = type;
//		if(isSingleWordEntity){			
//			lemmaArray = new String[1];
//			lemmaArray[0] = startLemma;
//		}
		this(startLemma, isSingleWordEntity, type);
		this.firstIndex = firstIndex;
		this.parent = parent;
	}

	
	
	public void addLabel(String label) {
		labels.add(label);
	}

	public void setCoordinates(String resolvedCoo) {
		List<String> gold = Arrays.asList(resolvedCoo.split(";"));
		this.coordinations = new ArrayList<String>();
		for (String g : gold) {
			this.coordinations.add(g.trim());
		}
	}
	
	/**
	 * transforms util.arraylist to array and asigns array to field
	 * @param lemmaArrayList
	 */
	public void setLemmaArrayList(List<String> lemmaArrayList) {
		this.lemmaArray = new String[lemmaArrayList.size()];
		lemmaArrayList.toArray(this.lemmaArray);
		
//		InformationEntity.sb = new StringBuilder();
		ExtractedEntity.sj = new StringJoiner(" ");
		for (String l : lemmaArray)
			sj.add(l);
//		sb.deleteCharAt(0);
		this.lemmaExpression = sj.toString();
	}
	
	/**
	 * get copy(!) of lemma array as util.arraylist
	 * @return
	 */
	public List<String> getLemmaArrayList() {		
		return new ArrayList<>(Arrays.asList(lemmaArray));
	}
	
	
	public void setLemmaArray(String[] lemmaArray) {
		this.lemmaArray = lemmaArray;
		
		ExtractedEntity.sj = new StringJoiner(" ");
		for (String l : lemmaArray)
			sj.add(l);
		this.lemmaExpression = sj.toString();
	}
	
	/**
	 * 
	 * appends a new lemma to the list of lemmata
	 * @param lemma
	 */
	@Deprecated
	public void addLemma(String lemma) {
		if (lemmaArrayList == null) {
			lemmaArrayList = new ArrayList<String>();
		}
		lemmaArrayList.add(lemma);
	}
	
	/**
	 * @return full expression of this IE
	 */
	@Override
	public String toString(){
		return lemmaExpression;
	}

	public void setPatternString(List<Pattern> patterns) {
		ExtractedEntity.sj = new StringJoiner("|", "[", "]");
		for (Pattern p : patterns)
			sj.add(p.getDescription());
		this.patternsString = sj.toString();
	}


}
