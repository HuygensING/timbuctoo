package nl.knaw.huygens.timbuctoo.server.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

// Container class, for entity reducer
class LocationNames {
  @JsonProperty("defLang")
  private String defLang;
  @JsonProperty("map")
  private Map<String, PlaceName> map;

  public LocationNames(String defLang) {
    this.defLang = defLang;
    map = Maps.newHashMap();
  }

  public LocationNames(){

  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  public void addCountryName(String lang, String name) {
    map.put(lang, new PlaceName().setCountry(name));
  }

  public String getDefaultName(LocationType locationType) {
    return map.get(defLang).getDisplayName(locationType);
  }

  public enum LocationType {
    UNKNOWN, DISTRICT, SETTLEMENT, REGION, COUNTRY, BLOC
  }

}
