package quenfo.de.uni_koeln.spinfo.classification.core.classifier.model;


import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import quenfo.de.uni_koeln.spinfo.classification.core.classifier.AbstractClassifier;
import quenfo.de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import quenfo.de.uni_koeln.spinfo.classification.core.feature_engineering.feature_weighting.AbstractFeatureQuantifier;

/**
 * @author geduldia
 * 
 * an abstract class for all models
 *  contains basic functionality of an model object
**/
 
//@Entity
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(name="type")
//@MappedSuperclass
@Data
public abstract class Model implements Serializable{
	
//	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
//	private long jpaID;
	
	
	private static final long serialVersionUID = 1L;

	/**
	 * the trainingSet this model is based on
	 */
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private File dataFile;
	
	/**
	 * the used FeatureUnitConfiguration
	 */
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private FeatureUnitConfiguration fuc;
	
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private String[] fUOrderArray;
	
	/**
	 * order of the FeatureUnits (translation of the Vector-Dimensions)
	 */
//	@DatabaseField(dataType = DataType.SERIALIZABLE)
	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private List<String> fUOrder;
	
	/**
	 * name of the corresponding classifier
	 */
	@DatabaseField(dataType = DataType.STRING)
	protected String classifierName;
	
	/**
	 * name of the used FeatureQuantifier
	 */
	@DatabaseField(dataType = DataType.STRING)
	private String fQName;
	
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private AbstractFeatureQuantifier fq;
	
	@DatabaseField(unique = true)
	private int configHash;
	
	
	public void setFUOrder(List<String> fUOrder) {		
		this.fUOrder = fUOrder;
		this.fUOrderArray = new String[fUOrder.size()];
		this.fUOrderArray = fUOrder.toArray(fUOrderArray);
	}
	
	
	public List<String> getFUOrder() {
		if (fUOrder == null)
			fUOrder = Arrays.asList(fUOrderArray);
		
		return fUOrder;
	}

	
	/**
	 * @return  an instance of the used FeatureQuantifier (specified in FQName)
	 */
	public AbstractFeatureQuantifier getFQ(){
		
		return fq;
	
//		if(fQName == null) {
//			;return null;
//		}
//		if(fQName.equals("LogLikeliHoodFeatureQuantifier")){
//			return new LogLikeliHoodFeatureQuantifier();
//		}
//		if(fQName.equals("TFIDFFeatureQuantifier")){
//			return new TFIDFFeatureQuantifier();
//		}
//		if(fQName.equals("AbsoluteFrequencyFeatureQuantifier")){
//			return new AbsoluteFrequencyFeatureQuantifier();
//		}
//		if(fQName.equals("RelativeFrequencyFeatureQuantifier")){
//			return new RelativeFrequencyFeatureQuantifier();
//		}
//		
//		return null;
	}



	
	public  AbstractClassifier getClassifier(){
		return null;
	}



}
