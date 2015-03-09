package nl.knaw.huygens.timbuctoo.tools.importer.cnw;

import java.util.Map;

import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.timbuctoo.model.cnw.AltName;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLink;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWPerson;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;

import com.google.common.collect.Maps;

public class PersonContext extends XmlContext {
	Map<String, String> locationIdMap = Maps.newHashMap();
	public CNWPerson person;

	public PersonName personName;
	String placeString;
	String country;
	public String birthPlaceId;
	public String deathPlaceId;
	public String year;
	public CNWLink link;
	public String nametype;
	String pid;
	public AltName currentAltName;
	public String currentRelationType;
	public String currentRelativeName;

	public PersonContext(String pid) {
		this.pid = pid;
	}

}
