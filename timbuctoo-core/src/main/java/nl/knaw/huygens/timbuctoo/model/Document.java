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
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Link;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

/**
 * <h1>Relation to Dublin Core Metadata</h1>
 * The Dublin Core metadata element set consists of 15 elements.
 * <table>
 * <tr><td>Title</td><td>the <code>title</code> property</td></tr>
 * <tr><td>Creator</td><td>implemented as relation to <code>Person</code>; see <code>getCreators()</code></td></tr>
 * <tr><td>Subject</td><td>implemented as relation to <code>Keyword</code>; see <code>getSubjects()</code></td></tr>
 * <tr><td>Description</td><td>the <code>description</code> property</td></tr>
 * <tr><td>Publisher</td><td>implemented as relation to <code>Collective</code></td></tr>
 * <tr><td>Contributor</td><td>not (yet) implemented</td></tr>
 * <tr><td>Date</td><td>the <code>date</code> property</td></tr>
 * <tr><td>Type</td><td>the <code>resourceType</code> property</td></tr>
 * <tr><td>Format</td><td>the <code>resourceFormat</code> property</td></tr>
 * <tr><td>Identifier</td><td>the <code>pid</code> domain entity property</td></tr>
 * <tr><td>Language</td><td>implemented as relation to <code>Language</code>; see <code>getLanguages()</code></td></tr>
 * <tr><td>Relation</td><td>not (yet) implemented</td></tr>
 * <tr><td>Coverage</td><td>not (yet) implemented</td></tr>
 * <tr><td>Rights</td><td>the <code>rights</code> property</td></tr>
 * </table>
 */
@IDPrefix("DOCU")
public class Document extends DomainEntity {

  public static enum DocumentType {
    UNKNOWN, ANTHOLOGY, ARTICLE, AWARD, CATALOGUE, COMPILATION, DIARY, LETTER, PERIODICAL, PICTURE, PUBLICITY, WORK
  }

  public static enum ResourceType {
    UNKNOWN, IMAGE, SOUND, TEXT
  }

  private String title;
  private String description;
  private String edition;
  private Datable date;
  private DocumentType documentType;
  private ResourceType resourceType;
  private String resourceFormat;
  private List<Link> links;
  private String reference;
  private String rights;

  public Document() {
    setDocumentType(null);
    setResourceType(null);
    links = Lists.newArrayList();
  }

  @Override
  public String getDisplayName() {
    return getTitle();
  }

  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_t_title", canBeEmpty = true),//
      @IndexAnnotation(fieldName = "dynamic_sort_title", canBeEmpty = true, isSortable = true) })
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  @IndexAnnotation(fieldName = "dynamic_s_date", canBeEmpty = true, isFaceted = true)
  public Datable getDate() {
    return date;
  }

  public void setDate(Datable date) {
    this.date = date;
  }

  @IndexAnnotation(fieldName = "dynamic_s_document_type", canBeEmpty = true, isFaceted = true)
  public DocumentType getDocumentType() {
    return documentType;
  }

  public void setDocumentType(DocumentType documentType) {
    this.documentType = (documentType == null) ? DocumentType.UNKNOWN : documentType;
  }

  public ResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(ResourceType resourceType) {
    this.resourceType = (resourceType == null) ? ResourceType.UNKNOWN : resourceType;
  }

  public String getResourceFormat() {
    return resourceFormat;
  }

  public void setResourceFormat(String resourceFormat) {
    this.resourceFormat = resourceFormat;
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

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getRights() {
    return rights;
  }

  public void setRights(String rights) {
    this.rights = rights;
  }

  @JsonIgnore
  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_s_creator", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true), //
      @IndexAnnotation(fieldName = "dynamic_sort_creator", accessors = { "getDisplayName" }, canBeEmpty = true, isSortable = true) })
  public List<EntityRef> getCreators() {
    return getRelations("isCreatedBy");
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_subject", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getSubjects() {
    return getRelations("hasSubject"); // undefined relation name, currently not used
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "dynamic_s_language", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getLanguages() {
    return getRelations("hasWorkLanguage");
  }

}
