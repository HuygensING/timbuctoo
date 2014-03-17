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
import nl.knaw.huygens.timbuctoo.model.util.Link;
import nl.knaw.huygens.timbuctoo.model.util.Period;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

/**
 * Any named collection of people regarded as a single unit.
 */
@IDPrefix("COLL")
public class Collective extends DomainEntity {

  private String type;
  private String name;
  private String acronym;
  private Period period;
  private List<Link> links;

  public Collective() {
    links = Lists.newArrayList();
    type = Type.UNKNOWN;
  }

  @Override
  public String getDisplayName() {
    return getName();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = Type.normalize(type);
  }

  @IndexAnnotation(fieldName = "dynamic_t_name", isFaceted = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAcronym() {
    return acronym;
  }

  public void setAcronym(String acronym) {
    this.acronym = acronym;
  }

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    this.period = period;
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
  @IndexAnnotation(fieldName = "dynamic_s_member", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = false)
  public List<EntityRef> getMembers() {
    return getRelations().get("has_member");
  }

  // ---------------------------------------------------------------------------

  public static class Type {
    public static final String UNKNOWN = "UNKNOWN";
    public static final String ACADEMY = "ACADEMY";
    public static final String ASSOCIATION = "ASSOCIATION";
    public static final String LIBRARY = "LIBRARY";
    public static final String PUBLISHER = "PUBLISHER";
    public static final String SHOP = "SHOP";

    public static String normalize(String text) {
      if (ACADEMY.equalsIgnoreCase(text)) {
        return ACADEMY;
      } else if (ASSOCIATION.equalsIgnoreCase(text)) {
        return ASSOCIATION;
      } else if (LIBRARY.equalsIgnoreCase(text)) {
        return LIBRARY;
      } else if (PUBLISHER.equalsIgnoreCase(text)) {
        return PUBLISHER;
      } else if (SHOP.equalsIgnoreCase(text)) {
        return SHOP;
      } else {
        return UNKNOWN;
      }
    }
  }

}
