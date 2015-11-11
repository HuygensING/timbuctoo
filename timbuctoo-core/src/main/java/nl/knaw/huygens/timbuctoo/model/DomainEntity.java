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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.annotations.DBProperty;
import nl.knaw.huygens.timbuctoo.annotations.JsonViews;
import nl.knaw.huygens.timbuctoo.config.BusinessRules;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.util.Text;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DomainEntity extends Entity {

  private static final List<RelationRef> NO_RELATIONS = ImmutableList.of();

  public static final String PID = "^pid";
  public static final String DELETED = "^deleted";
  public static final String VARIATIONS = "^variations";
  public static final String DB_PID_PROP_NAME = "pid";
  public static final String DB_VARIATIONS_PROP_NAME = "variations";

  @JsonProperty("^displayName")
  @DBProperty(value = "displayName", type = FieldType.ADMINISTRATIVE)
  private String displayName; // Used for demo purposes with Neo4J database.
  @DBProperty(value = DB_PID_PROP_NAME, type = FieldType.ADMINISTRATIVE)
  private String pid; // the persistent identifier.
  @DBProperty(value = "deleted", type = FieldType.ADMINISTRATIVE)
  private boolean deleted;
  @DBProperty(value = "relationCount", type = FieldType.VIRTUAL)
  private int relationCount;
  @DBProperty(value = "properties", type = FieldType.VIRTUAL)
  private final Map<String, Object> properties = Maps.newHashMap();
  @DBProperty(value = "relations", type = FieldType.VIRTUAL)
  private final Map<String, Set<RelationRef>> relations = Maps.newHashMap();
  @DBProperty(value = DB_VARIATIONS_PROP_NAME, type = FieldType.ADMINISTRATIVE)
  private List<String> variations = Lists.newArrayList();

  public DomainEntity() {
    relationCount = 0;
  }

  @JsonProperty(PID)
  @JsonView(JsonViews.NoExportView.class)
  public String getPid() {
    return pid;
  }

  @JsonProperty(PID)
  public void setPid(String pid) {
    this.pid = pid;
  }

  @JsonProperty(DELETED)
  @JsonView(JsonViews.NoExportView.class)
  public boolean isDeleted() {
    return deleted;
  }

  @JsonProperty(DELETED)
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  // ---------------------------------------------------------------------------

  /**
   * Returns the definition of properties that are added dynamically
   * to this entity (on demand).
   */
  @JsonIgnore
  public List<DerivedProperty> getDerivedProperties() {
    return ImmutableList.of();
  }

  @JsonProperty("@properties")
  public Map<String, Object> getProperties() {
    return properties;
  }

  @JsonIgnore
  public Object getProperty(String name) {
    return properties.get(name);
  }

  public void addProperty(String name, Object value) {
    properties.put(name, value);
  }

  // ---------------------------------------------------------------------------

  @JsonIgnore
  public List<DerivedRelationDescription> getDerivedRelationDescriptions() {
    return ImmutableList.of();
  }

  /**
   * Returns all relations of this entity.
   */
  @JsonProperty("@relations")
  public Map<String, Set<RelationRef>> getRelations() {
    return relations;
  }

  @JsonIgnore
  public List<RelationRef> getRelations(String name) {
    Set<RelationRef> refs = relations.get(name);
    return (refs != null) ? Lists.newArrayList(refs) : NO_RELATIONS;
  }

  @JsonProperty("@relationCount")
  @JsonView(JsonViews.NoExportView.class)
  public int getRelationCount() {
    return relationCount;
  }

  @JsonProperty("@relationCount")
  public void setRelationCount(int relationCount) {
    this.relationCount = relationCount;
  }

  public void addRelation(RelationRef ref) {
    String name = ref.getRelationName();
    relationCount++;
    Set<RelationRef> refs = relations.get(name);
    if (refs == null) {
      refs = Sets.newTreeSet();
      relations.put(name, refs);
    }
    refs.add(ref);
  }

  // TODO eliminate this
  public void clearRelations() {
    relationCount = 0;
    relations.clear();
  }

  // ---------------------------------------------------------------------------

  @JsonProperty(VARIATIONS)
  @JsonIgnore
  public List<String> getVariations() {
    return variations;
  }

  @JsonProperty(VARIATIONS)
  @JsonIgnore
  public void setVariations(List<String> variations) {
    this.variations = Lists.newArrayList();
    if (variations != null) {
      for (String variation : variations) {
        addVariation(variation);
      }
    }
  }

  public void addVariation(String variation) {
    if (!variations.contains(variation)) {
      variations.add(variation);
    }
  }

  public void addVariation(Class<? extends DomainEntity> type) {
    addVariation(TypeNames.getInternalName(type));
  }

  public boolean hasVariation(Class<? extends DomainEntity> type) {
    return variations.contains(TypeNames.getInternalName(type));
  }

  @JsonProperty("@variationRefs")
  public List<Reference> getVariationRefs() {
    List<Reference> refs = Lists.newArrayListWithCapacity(variations.size());
    for (String variation : variations) {
      refs.add(new Reference(variation, getId()));
    }
    return refs;
  }

  @Override
  public void validateForAdd(Repository repository) throws ValidationException {
    if (!BusinessRules.allowDomainEntityAdd(getClass())) {
      throw new ValidationException("Not allowed to add " + getClass());
    }
  }

  /**
   * Returns a map with key-value pairs, sorted by key, to be added to the client
   * representation of this entity, or {@code null} if there are no such data.
   */
  @JsonIgnore
  public Map<String, String> getClientRepresentation() {
    return null;
  }

  /**
   * Map the information from the index to a clean representation.
   *
   * The method should be overridden when the {@link #getClientRepresentation} is.
   * @param mappedIndexInformation the information from the client
   * @param <T> the type of the values of the client information map
   * @return the filtered map of with only the information analog to the client representation.
   */
  @JsonIgnore
  public <T> Map<String, T> createRelSearchRep(Map<String, T> mappedIndexInformation) {
    return Maps.newHashMap();
  }

  protected void addRelationToRepresentation(Map<String, String> data, String key, String relationName) {
    List<RelationRef> refs = getRelations(relationName);
    if (refs.size() == 0) {
      addItemToRepresentation(data, key, null);
    } else if (refs.size() == 1) {
      addItemToRepresentation(data, key, refs.get(0).getDisplayName());
    } else {
      Set<String> values = Sets.newTreeSet();
      for (RelationRef ref : refs) {
        values.add(ref.getDisplayName());
      }
      StringBuilder builder = new StringBuilder();
      for (String value : values) {
        Text.appendTo(builder, value, ";");
      }
      addItemToRepresentation(data, key, builder);
    }
  }

  protected void addItemToRepresentation(Map<String, String> data, String key, Object value) {
    data.put(key, (value != null) ? value.toString() : "");
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }


  protected  <T> void addValueToMap(Map<String, T> source, Map<String, T> target, String key) {
    target.put(key, source.get(key));
  }
}
