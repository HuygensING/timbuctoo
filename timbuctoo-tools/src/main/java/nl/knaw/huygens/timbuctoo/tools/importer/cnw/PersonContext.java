package nl.knaw.huygens.timbuctoo.tools.importer.cnw;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.timbuctoo.model.cnw.AltName;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLink;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLocation;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWPerson;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWRelation;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;
import nl.knaw.huygens.timbuctoo.tools.importer.RelationDTO;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PersonContext extends XmlContext {
	Map<String, String> locationIdMap = Maps.newHashMap();
	public CNWPerson person;
	public List<CNWRelation> relations = Lists.newArrayList();

	public PersonName personName;
	String placeString;
	String country;
	public String birthPlaceId;
	public String deathPlaceId;
	public String year;
	public CNWLink link;
	public String nametype;
	String pid;
	public CNWRelation currentRelation;
	public AltName currentAltName;
	public String currentRelationType;
	public String currentRelativeName;

	public PersonContext(String pid) {
		this.pid = pid;
	}

	protected String getCurrentLocationId() {
		if (country == null) {
			country = "";
		}
		if (placeString == null) {
			placeString = "";
		}
		String locationKey = country + "+" + placeString;
		if (locationKey.equals("+")) {
			return null;
		}
		if (locationIdMap.containsKey(locationKey)) {
			return locationIdMap.get(locationKey);
		}
		CNWLocation location = new CNWLocation();
		PlaceName name = new PlaceName();
		name.setCountry(country);
		name.setSettlement(placeString);
		location.addName("Nld", name);
		String locationId = "cnw:location:" + locationKey;
		locationIdMap.put(locationKey, locationId);
		location.setId(locationId);
		country = "";
		placeString = "";
		return locationId;
	}

}
