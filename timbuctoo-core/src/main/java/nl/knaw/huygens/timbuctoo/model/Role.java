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

import nl.knaw.huygens.timbuctoo.config.TimbuctooTypeIdResolver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * An abstract super class to define "functions" of entities.
 * E.g., a {@code Person} could have the role {@code Scientist}.
 */
// Annotations determine to which subclass the entity has to be resolved.
// @see http://wiki.fasterxml.com/JacksonPolymorphicDeserialization
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeIdResolver(value = TimbuctooTypeIdResolver.class)
public abstract class Role implements Variable {

  private List<Reference> variationRefs;

  @Override
  @JsonProperty("@variationRefs")
  //Should ignore the variationRefs during deserialization.
  @JsonIgnoreProperties(ignoreUnknown = true)
  public List<Reference> getVariationRefs() {
    return variationRefs;
  }

}
