package quenfo.de.uni_koeln.spinfo.classification.core.data;

import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
 * @author geduldia
 * 
 * represents a basic classification-object
 *
 */

@Data
@EqualsAndHashCode(of = {"id"})
public class ClassifyUnit {
	
	@ToString.Exclude
	protected String content;
	
	//@EqualsAndHashCode.Include
	protected UUID id;
	
	/**
	 * list of features
	 */
	@ToString.Exclude
	private List<String> featureUnits;
	
	/**
	 * weighted document vector
	 */
	@ToString.Exclude
	private double[] featureVector;
	
	
	
	/**
	 * @param content
	 * @param id
	 */
	public ClassifyUnit(String content, UUID id){
		this.id = id;
		this.content = content;
	}
	
	/**
	 * 
	 * @param content
	 */
	public ClassifyUnit(String content){
		this(content, UUID.randomUUID());
	}
	
//	/**
//	 * 
//	 * @return featureUnits
//	 */
//	public List<String> getFeatureUnits() {
//		return FeatureUnits;
//	}
//
//	/**
//	 * 
//	 * @param content
//	 */
//	public void setContent(String content){
//		this.content = content;
//	}
//	/**
//	 * 
//	 * @param featureUnits
//	 */
//	public void setFeatureUnits(List<String> featureUnits) {
//		FeatureUnits = featureUnits;
//	}
//	
//	/**
//	 * 
//	 * @return feature-vector
//	 */
//	public double[] getFeatureVector() {
//		return FeatureVector;
//	}
//	
//	/**
//	 * 
//	 * @param featureVector
//	 */
//	public void setFeatureVector(double[] featureVector) {
//		FeatureVector = featureVector;
//	}
//	
//	/**
//	 * 
//	 * @return id
//	 */
//	public UUID getID(){
//		return id;
//	}
//
//	/**
//	 * 
//	 * @return content
//	 */
//	public String getContent() {
//		return content;
//	}
//	
//	@Override
//	public boolean equals(Object other){
//		if(id.equals(((ClassifyUnit)other).id)){
//			return true;
//		}
//		return false;
//		
//	}
//	
//	@Override
//	public int hashCode(){
//		return id.hashCode();
//	}

}
