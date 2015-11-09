package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.annotations.DBProperty;
import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.annotations.RawSearchField;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

@IDPrefix("LOCA")
@RawSearchField(Location.INDEX_FIELD_NAME)
public class Location extends DomainEntity {

  static final String INDEX_FIELD_NAME = "dynamic_t_name";
  public static final String URN = "^urn";
  public static final String LATITUDE = "latitude";
  public static final String LONGITUDE = "longitude";

  public static enum LocationType {
    UNKNOWN, DISTRICT, SETTLEMENT, REGION, COUNTRY, BLOC
  }

  // Container class, for entity reducer
  private static class Names {
    @DBProperty(value = "deflang", type = FieldType.ADMINISTRATIVE)
    public String defLang;
    public Map<String, PlaceName> map;

    public Names() {
      map = Maps.newHashMap();
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this, false);
    }
  }

  @DBProperty(value = "locationType", type = FieldType.ADMINISTRATIVE)
  private LocationType locationType;
  @DBProperty(value = "names", type = FieldType.ADMINISTRATIVE)
  private Names names;
  @JsonProperty(LATITUDE)
  private String latitude;
  @JsonProperty(LONGITUDE)
  private String longitude;
  /** URN for making concordances. */
  private String urn;

  public Location() {
    setLocationType(null);
    names = new Names();
  }

  @Override
  public String getIdentificationName() {
    if (names.defLang != null) {
      PlaceName placeName = names.map.get(names.defLang);
      if (placeName != null) {
        return placeName.getDisplayName(locationType);
      }
    }
    return "undefined";
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = INDEX_FIELD_NAME, isFaceted = false)
  public String getIndexedName() {
    StringBuilder builder = new StringBuilder();
    boolean isFirst = true;
    for (PlaceName name : names.map.values()) {
      if (!isFirst) {
        builder.append(' ');
      } else {
        isFirst = false;
      }
      builder.append(name.getDisplayName(locationType));
    }
    return builder.toString();
  }

  @JsonProperty("^locationType")
  @IndexAnnotation(fieldName = "dynamic_s_location_type", canBeEmpty = true, isFaceted = true)
  public LocationType getLocationType() {
    return locationType;
  }

  @JsonProperty("^locationType")
  public void setLocationType(LocationType type) {
    locationType = (type == null) ? LocationType.UNKNOWN : type;
  }

  @JsonProperty("^defLang")
  public String getDefLang() {
    return names.defLang;
  }

  @JsonProperty("^defLang")
  public void setDefLang(String value) {
    names.defLang = value;
  }

  @JsonProperty("^names")
  public Map<String, PlaceName> getNames() {
    return names.map;
  }

  @JsonProperty("^names")
  public void setNames(Map<String, PlaceName> value) {
    names.map = value;
    setDisplayName(getIndexedName());

  }

  public void addName(String lang, PlaceName name) {
    names.map.put(lang, name);
  }

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  };

  @JsonProperty(URN)
  public String getUrn() {
    return urn;
  }

  @JsonProperty(URN)
  public void setUrn(String urn) {
    this.urn = urn;
  }

}
