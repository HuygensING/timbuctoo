package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo REST api
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

import com.google.common.base.Joiner;
import nl.knaw.huygens.timbuctoo.config.Paths;

import java.util.Map;

public class DomainEntityDTO {

  private String type;
  private String id;
  private String path;
  private String displayName;
  private Map<String, ? extends Object> data;

  public DomainEntityDTO(String type, String xtype, DomainEntity entity) {
    this.type = type;
    id = entity.getId();
    path = Joiner.on('/').join(Paths.DOMAIN_PREFIX, xtype, id);
    displayName = entity.getIdentificationName();
    data = entity.getClientRepresentation();
  }

  public DomainEntityDTO() {
  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Map<String, ? extends Object> getData() {
    return data;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setData(Map<String, ? extends Object> data) {
    this.data = data;
  }
}
