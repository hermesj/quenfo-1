package quenfo.de.uni_koeln.spinfo.classification.zone_analysis.classifier.model;

import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import quenfo.de.uni_koeln.spinfo.classification.core.classifier.AbstractClassifier;
import quenfo.de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import quenfo.de.uni_koeln.spinfo.classification.zone_analysis.classifier.ZoneKNNClassifier;

/**
 * @author geduldia
 * 
 * a model-object based on the KNNClassifier
 *
 */
@DatabaseTable
public class ZoneKNNModel extends Model {
	
	


	private static final long serialVersionUID = 1L;
	
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private HashMap<double[], boolean[]> trainingData = new HashMap<double[], boolean[]>();
	
	
	
	/**
	 * @return trainingData
	 */
	public Map<double[], boolean[]> getTrainingData() {
		return trainingData;
	}
	
	


	/**
	 * @param trainingData
	 */
	public void setTrainingData(HashMap<double[], boolean[]> trainingData){
		this.trainingData = trainingData;
	}


	public AbstractClassifier getClassifier(){
		return new ZoneKNNClassifier();
	}
	
	

}
