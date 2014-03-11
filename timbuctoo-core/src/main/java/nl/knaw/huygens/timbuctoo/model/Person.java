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

import java.util.List;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Link;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

@IDPrefix("PERS")
public class Person extends DomainEntity {

  private List<String> types;
  private PersonName name;
  /** Gender at birth. */
  private String gender;
  private Datable birthDate;
  private Datable deathDate;
  private List<Link> links;

  public Person() {
    name = new PersonName();
    links = Lists.newArrayList();
    types = Lists.newArrayList();
    gender = Gender.UNKNOWN;
  }

  @Override
  public String getDisplayName() {
    return name.getShortName();
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_t_name", isFaceted = false)
  public String getIndexedName() {
    return name.getFullName();
  }

  public List<String> getTypes() {
    return types;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }

  public void addType(String type) {
    String normalized = Type.normalize(type);
    if (normalized != null && !types.contains(normalized)) {
      types.add(normalized);
    }
  }

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }

  @IndexAnnotation(fieldName = "dynamic_s_gender", isFaceted = true, canBeEmpty = true)
  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = Gender.normalize(gender);
  }

  @IndexAnnotation(fieldName = "dynamic_s_birthDate", isFaceted = true, canBeEmpty = true)
  public Datable getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Datable birthDate) {
    this.birthDate = birthDate;
  }

  @IndexAnnotation(fieldName = "dynamic_s_deathDate", isFaceted = true, canBeEmpty = true)
  public Datable getDeathDate() {
    return deathDate;
  }

  public void setDeathDate(Datable deathDate) {
    this.deathDate = deathDate;
  }

  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }

  public void addLink(Link link) {
    if (link != null) {
      links.add(link);
    }
  }

  // ---------------------------------------------------------------------------

  // Not an enumerated type because of serialization problems.
  public static class Type {
    public static final String ARCHETYPE = "ARCHETYPE";
    public static final String AUTHOR = "AUTHOR";
    public static final String PSEUDONYM = "PSEUDONYM";

    public static String normalize(String text) {
      if (ARCHETYPE.equalsIgnoreCase(text)) {
        return ARCHETYPE;
      } else if (AUTHOR.equalsIgnoreCase(text)) {
        return AUTHOR;
      } else if (PSEUDONYM.equalsIgnoreCase(text)) {
        return PSEUDONYM;
      } else {
        return null;
      }
    }
  }

  // Not an enumerated type because of serialization problems.
  public static class Gender {
    public static final String UNKNOWN = "UNKNOWN";
    public static final String MALE = "MALE";
    public static final String FEMALE = "FEMALE";
    public static final String NOT_APPLICABLE = "NOT_APPLICABLE";

    public static String normalize(String text) {
      if (MALE.equalsIgnoreCase(text)) {
        return MALE;
      } else if (FEMALE.equalsIgnoreCase(text)) {
        return FEMALE;
      } else if (NOT_APPLICABLE.equalsIgnoreCase(text)) {
        return NOT_APPLICABLE;
      } else {
        return UNKNOWN;
      }
    }
  }

}
