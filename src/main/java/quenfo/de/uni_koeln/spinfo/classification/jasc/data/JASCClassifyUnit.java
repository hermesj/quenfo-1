package quenfo.de.uni_koeln.spinfo.classification.jasc.data;

import java.util.List;
import java.util.Map;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.helpers.SingleToMultiClassConverter;
import quenfo.de.uni_koeln.spinfo.core.data.JobAd;

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

@DatabaseTable(tableName = "ClassifiedParagraphs")
@Data
@ToString(of = {}, callSuper = true)
@EqualsAndHashCode(of = {}, callSuper=true)
public class JASCClassifyUnit extends ClassifyUnit {
	

	private static int NUMBEROFSINGLECLASSES;
	private static int NUMBEROFMULTICLASSES;
	private static SingleToMultiClassConverter CONVERTER;
	
	
	@DatabaseField(dataType=DataType.SERIALIZABLE)
	@Setter(AccessLevel.NONE)
	boolean[] classIDs;
	
	@DatabaseField(columnName = "ClassONE")
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	boolean classOne;
	
	@DatabaseField(columnName = "ClassTWO")
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	boolean classTwo;
	
	@DatabaseField(columnName = "ClassTHREE")
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	boolean classThree;
	
	@DatabaseField(columnName = "ClassFOUR")
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	boolean classFour;
	
	/**
	 * default constructor for object relational mapping
	 */
	public JASCClassifyUnit() {

	}

	@Deprecated
	public JASCClassifyUnit(String content, String jahrgang, String postingID){
		this(content, jahrgang, postingID, -1);
	}
	
	
	public JASCClassifyUnit(String content, JobAd jobad) {
		this(content, jobad, -1);
	}

	/**
	 * constructor for training data classify units
	 * @param content
	 * @param jahrgang
	 * @param postingID
	 * @param id
	 * @param actualClassID
	 */
	@Deprecated
	public JASCClassifyUnit(String content, String jahrgang, String postingID, int actualClassID) {
		super(content, jahrgang, postingID);
		setActualClassID(actualClassID);
//		System.out.println(actualClassID);
	}
	
	
	public JASCClassifyUnit(String content, JobAd jobad, int actualClassID) {
		super(content, jobad);
		setActualClassID(actualClassID);
	}

	



	public int transformToClassID() {
		
		if (classIDs == null)
			return -1;
		
		if (CONVERTER != null) {
			return CONVERTER.getSingleClass(classIDs);
		} else {
			for (int i = 0; i < classIDs.length; i++) {
				if (classIDs[i]) {
					return (i + 1);
				}
			}
		}
		
		return -1;
	}
	
	
	public void setClassIDs(boolean[] classIDs) {
		
//		System.out.println(actualClassID + " " + classIDs);

		if (classIDs == null)
			return;

		this.classIDs = classIDs;
		
		classOne = classIDs[0];
		classTwo = classIDs[1];
		classThree = classIDs[2];
		classFour = classIDs[3];
//		if (actualClassID == -1) {
//			if (CONVERTER != null) {
//				actualClassID = CONVERTER.getSingleClass(classIDs);
//			} else {
//				for (int i = 0; i < classIDs.length; i++) {
//					if (classIDs[i]) {
//						actualClassID = i + 1;
//						return;
//					}
//				}
//			}
//		}
//		for (boolean id : classIDs) {
//			System.out.print(id + " ");
//		}
//		System.out.println("--> ClassID: " + actualClassID);
	}

	/**
	 * ... for training data
	 * @param classID
	 */
	public void setActualClassID(int classID) {
//		this.actualClassID = classID;
		if (classIDs == null) {
			if (CONVERTER != null) {
				classIDs = CONVERTER.getMultiClasses(classID);
			} else {
				classIDs = new boolean[NUMBEROFSINGLECLASSES];
				if (classID > 0) {
					classIDs[classID - 1] = true;
				}
			}
			
			
			classOne = classIDs[0];
			classTwo = classIDs[1];
			classThree = classIDs[2];
			classFour = classIDs[3];
			
		}

	}
	

	
	
	public static void setNumberOfCategories(int categoriesNo, int classesNo,
			Map<Integer, List<Integer>> translations) {
		NUMBEROFMULTICLASSES = categoriesNo;
		NUMBEROFSINGLECLASSES = classesNo;
		CONVERTER = new SingleToMultiClassConverter(NUMBEROFSINGLECLASSES, NUMBEROFMULTICLASSES, translations);
	}


	
}
