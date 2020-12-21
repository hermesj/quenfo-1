package quenfo.de.uni_koeln.spinfo.information_extraction.data;

import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@DatabaseTable(tableName = "Matches")
@Data
@EqualsAndHashCode(of = {}, callSuper=true)
public class MatchedEntity extends InformationEntity {

}
