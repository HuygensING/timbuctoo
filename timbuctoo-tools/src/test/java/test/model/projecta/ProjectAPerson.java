package test.model.projecta;

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

import nl.knaw.huygens.timbuctoo.model.Person;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectAPerson extends Person {
  public static final String PROJECT_A_PERSON_PROPERTY_NAME = "projectAPersonProperty";
  @JsonProperty(PROJECT_A_PERSON_PROPERTY_NAME)
  private String projectAPersonProperty;

  public String getProjectAPersonProperty() {
    return projectAPersonProperty;
  }

  public void setProjectAPersonProperty(String projectAPersonProperty) {
    this.projectAPersonProperty = projectAPersonProperty;
  }
}
