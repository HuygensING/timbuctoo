package nl.knaw.huygens.timbuctoo.variation.model.projecta;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.variation.model.TestConcreteDoc;

import com.google.common.base.Objects;

public class ProjectATestDocWithPersonName extends TestConcreteDoc {
  private PersonName personName;

  public PersonName getPersonName() {
    return personName;
  }

  public void setPersonName(PersonName personName) {
    this.personName = personName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProjectATestDocWithPersonName)) {
      return false;
    }
    ProjectATestDocWithPersonName other = (ProjectATestDocWithPersonName) obj;

    return Objects.equal(other.personName, personName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(personName);
  }

  @Override
  public String toString() {
    return "ProjectATestDocWithPersonName{\npersonName: " + personName + "\n}";
  }
}
