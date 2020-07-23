package quenfo.de.uni_koeln.spinfo.classification.jasc.data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.helpers.SingleToMultiClassConverter;

/** 
 * A basic unit for all classify tasks.
 * 
 * @author jhermes
 *
 */
/**
 * @author geduldia
 *
 */
//@Entity
//@Inheritance(strategy = InheritanceType.JOINED)
//@MappedSuperclass
@Data
@ToString(of = {}, callSuper = true)
@EqualsAndHashCode(of = {}, callSuper=true)
public class JASCClassifyUnit extends ClassifyUnit {
	

	private static int NUMBEROFSINGLECLASSES;
	private static int NUMBEROFMULTICLASSES;
	private static SingleToMultiClassConverter CONVERTER;
	

	@Setter(AccessLevel.NONE)
	private int actualClassID; 
	
	@Setter(AccessLevel.NONE)
	boolean[] classIDs;
	
	
	
	
	
	
	/**
	 * default constructor for object relational mapping
	 */
	public JASCClassifyUnit() {

	}

	
	public JASCClassifyUnit(String content, int jahrgang, String postingID){
		this(content, jahrgang, postingID, UUID.randomUUID(), -1);
	}

	/**
	 * constructor for training data classify units
	 * @param content
	 * @param jahrgang
	 * @param postingID
	 * @param id
	 * @param actualClassID
	 */
	public JASCClassifyUnit(String content, int jahrgang, String postingID, UUID id, int actualClassID) {
		super(content,id, jahrgang, postingID);
		setActualClassID(actualClassID);
	}
	
	
	public static void setNumberOfCategories(int categoriesNo, int classesNo,
			Map<Integer, List<Integer>> translations) {
		NUMBEROFMULTICLASSES = categoriesNo;
		NUMBEROFSINGLECLASSES = classesNo;
		CONVERTER = new SingleToMultiClassConverter(NUMBEROFSINGLECLASSES, NUMBEROFMULTICLASSES, translations);
	}
	
	
	public void setClassIDs(boolean[] classIDs) {

		if (classIDs == null)
			return;

		this.classIDs = classIDs;
		if (actualClassID == -1) {
			if (CONVERTER != null) {
				actualClassID = CONVERTER.getSingleClass(classIDs);
			} else {
				for (int i = 0; i < classIDs.length; i++) {
					if (classIDs[i]) {
						actualClassID = i + 1;
						return;
					}
				}
			}
		}
	}

	public void setActualClassID(int classID) {
		this.actualClassID = classID;
		if (classIDs == null) {
			if (CONVERTER != null) {
				classIDs = CONVERTER.getMultiClasses(classID);
			} else {
				classIDs = new boolean[NUMBEROFSINGLECLASSES];
				if (classID > 0) {
					classIDs[classID - 1] = true;
				}
			}
		}

	}

	public void setClassIDsAndActualClassID(boolean[] classIDs) {
		// TODO JB: synchronisierung zwischen Array und Int ClassID kaputt
		if (classIDs == null)
			return;

		this.classIDs = classIDs;

		if (CONVERTER != null) {
			actualClassID = CONVERTER.getSingleClass(classIDs);
		} else {
			for (int i = 0; i < classIDs.length; i++) {
				if (classIDs[i]) {
					actualClassID = i + 1;
					return;
				}
			}
		}

	}

	
}
