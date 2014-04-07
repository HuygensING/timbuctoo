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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.BusinessRules;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.validation.ValidationException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class DomainEntity extends Entity implements Variable {

  public static final String PID = "^pid";
  public static final String DELETED = "^deleted";
  public static final String ROLES = "^roles";
  public static final String VARIATIONS = "^variations";

  private String pid; // the persistent identifier.
  private boolean deleted;
  private int relationCount;
  private Map<String, List<EntityRef>> relations = Maps.newHashMap();
  private List<String> variations = Lists.newArrayList();
  private List<Role> roles = Lists.newArrayList();

  public DomainEntity() {
    relationCount = 0;
  }

  @JsonProperty(PID)
  public String getPid() {
    return pid;
  }

  @JsonProperty(PID)
  public void setPid(String pid) {
    this.pid = pid;
  }

  @JsonProperty(DELETED)
  public boolean isDeleted() {
    return deleted;
  }

  @JsonProperty(DELETED)
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @JsonProperty("@relations")
  public Map<String, List<EntityRef>> getRelations() {
    return relations;
  }

  @JsonProperty("@relationCount")
  public int getRelationCount() {
    return relationCount;
  }

  @JsonProperty("@relationCount")
  public void setRelationCount(int relationCount) {
    this.relationCount = relationCount;
  }

  public void addRelation(String name, EntityRef ref) {
    relationCount++;
    List<EntityRef> refs = relations.get(name);
    if (refs == null) {
      refs = Lists.newArrayList();
      relations.put(name, refs);
    }
    refs.add(ref);
  }

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

  public void addVariation(Class<?> type) {
    addVariation(TypeNames.getInternalName(type));
  }

  @Override
  @JsonProperty("@variationRefs")
  public List<Reference> getVariationRefs() {
    List<Reference> refs = Lists.newArrayListWithCapacity(variations.size());
    for (String variation : variations) {
      refs.add(new Reference(variation, getId()));
    }
    return refs;
  }

  @JsonProperty(ROLES)
  public List<Role> getRoles() {
    return roles;
  }

  @JsonProperty(ROLES)
  public void setRoles(List<Role> roles) {
    this.roles = checkNotNull(roles);
  }

  public void addRole(Role role) {
    roles.add(role);
  }

  @Override
  public void validateForAdd(TypeRegistry registry, Storage storage) throws ValidationException {
    if (!BusinessRules.allowDomainEntityAdd(getClass())) {
      throw new ValidationException("Not allowed to add " + getClass());
    }
  }

}
