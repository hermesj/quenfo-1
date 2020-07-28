package quenfo.de.uni_koeln.spinfo.core.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

//@Entity
@DatabaseTable(tableName = "jobads")
@Data
@EqualsAndHashCode(of = {"postingID", "jahrgang"}) 
@ToString(of = {"postingID", "jahrgang"})
public class JobAd {
	
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private long jpaID;
	
//	@Lob
	
	@DatabaseField(uniqueCombo = true)
	private String postingID;
	
	@DatabaseField(uniqueCombo = true)
	private int jahrgang;
	
	@DatabaseField()
	private String language;
	
	@DatabaseField()
	private String content;
	
//	/**
//	 * speichert mit welchen Konfigurationen vorverarbeitet wurde
//	 */
//	private String preprocessingConfig;
//	
//	private List<String> featureUnits;
//	
//	private double[] featureVector;
	
	//private Vector sparseVector;
	
	
	public JobAd() {
		
	}
	
	public JobAd(String content, String postingID, int jahrgang) {
		this.content = content;
		this.postingID = postingID;
		this.jahrgang = jahrgang;
	}
	

}
