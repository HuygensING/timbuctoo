package nl.knaw.huygens.timbuctoo.model.ckcc;

/*
 * #%L
 * Timbuctoo model
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

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Collective;

import java.util.Map;

public class CKCCCollective extends Collective {

  public static final String URN = "urn";
  /** Unique identifier for concordances */
  private String urn;

  public String getUrn() {
    return urn;
  }

  public void setUrn(String urn) {
    this.urn = urn;
  }

  @Override
  public Map<String, String> getClientRepresentation() {
    Map<String, String> data = Maps.newTreeMap();
    addItemToRepresentation(data, URN, getUrn());
    return data;
  }

  @Override
  public <T> Map<String, T> createRelSearchRep(Map<String, T> mappedIndexInformation) {
    Map<String, T> filteredMap = Maps.newTreeMap();
    addValueToMap(mappedIndexInformation, filteredMap, URN);

    return super.createRelSearchRep(mappedIndexInformation);
  }
}
