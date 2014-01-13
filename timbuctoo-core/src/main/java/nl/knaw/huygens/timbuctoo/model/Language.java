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

import com.google.common.collect.Maps;

/**
 * Denotes a language, catering for ISO 639-2 and 639-1 codes.
 */
@IDPrefix("LANG")
public class Language extends DomainEntity {

  /** Codes, e.g. "iso_639_2". */
  private Map<String, String> codes;
  /** English name. */
  private String name;

  public Language() {
    codes = Maps.newHashMapWithExpectedSize(2);
  }

  @Override
  public String getDisplayName() {
    return getName();
  }

  public Map<String, String> getCodes() {
    return codes;
  }

  public void setCodes(Map<String, String> codes) {
    this.codes = codes;
  }

  public void addCode(String key, String value) {
    codes.put(key, value);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
