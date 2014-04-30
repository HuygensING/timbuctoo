package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.util.Map;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

@IDPrefix("LOCA")
public class Location extends DomainEntity {

  public static final String URN = "^urn";

  // Container class, for entity reducer
  private static class Names {
    public String defLang;
    public Map<String, PlaceName> map;
    public Names() {
      map = Maps.newHashMap();
    }
  }

  /** URN for making concordances. */
  private String urn;
  private Names names;
  private String latitude;
  private String longitude;

  public Location() {
    names = new Names();
  }

  @Override
  public String getDisplayName() {
    if (names.defLang != null) {
      PlaceName placeName = names.map.get(names.defLang);
      if (placeName != null) {
        return placeName.getLongName();
      }
    }
    return "undefined";
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_t_name", isFaceted = false)
  public String getIndexedName() {
    StringBuilder builder = new StringBuilder();
    for (PlaceName name : names.map.values()) {
      builder.append(' ').append(name.getLongName());
    }
    return builder.toString();
  }

  @JsonProperty(URN)
  public String getUrn() {
    return urn;
  }

  @JsonProperty(URN)
  public void setUrn(String urn) {
    this.urn = urn;
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

}
