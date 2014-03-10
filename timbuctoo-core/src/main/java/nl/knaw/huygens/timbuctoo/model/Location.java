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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;

@IDPrefix("LOCA")
public class Location extends DomainEntity {

  /** URN for making concordances. */
  private String urn;
  /** Default place name language. */
  private String defLang;
  private Map<String, PlaceName> names = Maps.newHashMap();
  private String latitude;
  private String longitude;

  @Override
  public String getDisplayName() {
    return null;
  }

  @JsonProperty("^urn")
  public String getUrn() {
    return urn;
  }

  @JsonProperty("^urn")
  public void setUrn(String urn) {
    this.urn = urn;
  }

  @JsonProperty("^defLang")
  public String getDefLang() {
    return defLang;
  }

  @JsonProperty("^defLang")
  public void setDefLang(String defLang) {
    this.defLang = defLang;
  }

  @JsonProperty("^names")
  public Map<String, PlaceName> getNames() {
    return names;
  }

  @JsonProperty("^names")
  public void setNames(Map<String, PlaceName> names) {
    this.names = names;
  }

  public void addName(String lang, PlaceName name) {
    names.put(lang, name);
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
