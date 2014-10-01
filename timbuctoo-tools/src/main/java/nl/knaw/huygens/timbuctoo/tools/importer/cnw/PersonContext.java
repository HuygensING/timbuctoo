package nl.knaw.huygens.timbuctoo.tools.importer.cnw;

import java.util.Map;

import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLink;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLocation;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWPerson;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;

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
		String locationId = locationKey;
		locationIdMap.put(locationKey, locationId);
		country = "";
		placeString = "";
		return locationId;
	}

}
