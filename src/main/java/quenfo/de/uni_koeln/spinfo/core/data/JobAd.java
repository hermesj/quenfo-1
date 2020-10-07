package quenfo.de.uni_koeln.spinfo.core.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

@DatabaseTable(tableName = "jobads")
@Data
@EqualsAndHashCode(of = {"id"}) 
@ToString(of = {"id"})
public class JobAd {

	@DatabaseField(id = true)
//	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private Integer id;
	
	@DatabaseField(uniqueCombo = true)
	private String postingID;
	
	@DatabaseField(uniqueCombo = true)
	private String jahrgang;
	
	@DatabaseField()
	private String language;
	
	@DatabaseField()
	private String content;
	
	
	
	public JobAd() {
		
	}
	
	public JobAd(String content, String postingID, String jahrgang) {
		this.content = content;
		this.postingID = postingID;
		this.jahrgang = jahrgang;
	}
	

}
