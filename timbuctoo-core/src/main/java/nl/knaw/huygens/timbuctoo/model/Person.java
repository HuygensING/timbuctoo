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

import java.util.List;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.FloruitPeriod;
import nl.knaw.huygens.timbuctoo.model.util.Link;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.util.Text;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

@IDPrefix("PERS")
public class Person extends DomainEntity {

  public static enum Gender {
    UNKNOWN, MALE, FEMALE, NOT_APPLICABLE
  }

  // Container class, for entity reducer
  private static class Names {
    public List<PersonName> list;

    public Names() {
      list = Lists.newArrayList();
    }

    public PersonName defaultName() {
      return (list != null && !list.isEmpty()) ? list.get(0) : new PersonName();
    }
  }

  private final Names names;
  /** Gender at birth. */
  private Gender gender;
  private Datable birthDate;
  private Datable deathDate;
  private List<String> types;
  private List<Link> links;
  private FloruitPeriod floruit;

  public Person() {
    names = new Names();
    gender = Gender.UNKNOWN;
    types = Lists.newArrayList();
    links = Lists.newArrayList();
  }

  protected PersonName defaultName() {
    return names.defaultName();
  }

  @Override
  public String getIdentificationName() {
    return defaultName().getShortName() + period();
  }

  private String period() {
    Datable birthDate = getBirthDate();
    Datable deathDate = getDeathDate();

    StringBuilder builder = new StringBuilder();
    if (birthDate != null || deathDate != null) {
      builder.append(" (");
      if (birthDate != null) {
        builder.append(getBirthYear(birthDate));
      }
      builder.append('-');
      if (deathDate != null) {
        builder.append(getDeathYear(deathDate));
      }
      builder.append(')');
    } else if (floruit != null) {
      builder.append(" (").append(floruit).append(')');
    }
    return builder.toString();
  }

  private int getDeathYear(Datable deathDate) {
    int deathYear = 0;
    if (deathDate.getToDate() != null) {
      deathYear = deathDate.getToYear();
    } else {
      if (deathDate.getFromDate() != null) {
        deathYear = deathDate.getFromYear();
      }
    }
    return deathYear;
  }

  private int getBirthYear(Datable birthDate) {
    int birthYear = 0;
    if (birthDate.getFromDate() != null) {
      birthYear = birthDate.getFromYear();
    } else {
      if (birthDate.getToDate() != null) {
        birthYear = birthDate.getToYear();
      }
    }
    return birthYear;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_t_name", isFaceted = false)
  public String getIndexedName() {
    StringBuilder builder = new StringBuilder();
    for (PersonName name : getNames()) {
      Text.appendTo(builder, name.getFullName(), " ");
    }
    return builder.toString();
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_sort_name", isFaceted = false, isSortable = true)
  public String getSortName() {
    return defaultName().getSortName();
  }

  @IndexAnnotation(fieldName = "dynamic_s_types", isFaceted = true)
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

  public List<PersonName> getNames() {
    return names.list;
  }

  public void setNames(List<PersonName> names) {
    this.names.list = names;
  }

  public void addName(PersonName name) {
    if (name != null) {
      names.list.add(name);
    }
  }

  @IndexAnnotation(fieldName = "dynamic_s_gender", isFaceted = true, canBeEmpty = true)
  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_s_birthDate", isFaceted = true, canBeEmpty = true), //
      @IndexAnnotation(fieldName = "dynamic_k_birthDate", canBeEmpty = true, isSortable = true) })
  public Datable getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Datable birthDate) {
    this.birthDate = birthDate;
  }

  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_s_deathDate", isFaceted = true, canBeEmpty = true), //
      @IndexAnnotation(fieldName = "dynamic_k_deathDate", isSortable = true, canBeEmpty = true) })
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

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_birthplace", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getBirthPlace() {
    return getRelations("hasBirthPlace");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_deathplace", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<RelationRef> getDeathPlace() {
    return getRelations("hasDeathPlace");
  }

  // ---------------------------------------------------------------------------

  public FloruitPeriod getFloruit() {
    return floruit;
  }

  public void setFloruit(FloruitPeriod floruit) {
    this.floruit = floruit;
  }

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

}
