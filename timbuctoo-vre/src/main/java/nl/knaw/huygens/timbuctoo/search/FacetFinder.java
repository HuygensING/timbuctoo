package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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

import java.lang.reflect.Method;
import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetDefinition;
import nl.knaw.huygens.facetedsearch.model.FacetDefinitionBuilder;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Lists;

public class FacetFinder {

  private static final Class<IndexAnnotations> INDEX_ANNOTATIONS_CLASS = IndexAnnotations.class;
  private static final Class<IndexAnnotation> INDEX_ANNOTATION_CLASS = IndexAnnotation.class;

  public List<FacetDefinition> findFacetDefinitions(Class<? extends Entity> type) {
    List<FacetDefinition> facetDefinitions = Lists.newArrayList();
    Method[] methods = type.getMethods();

    for (Method method : methods) {
      if (method.isAnnotationPresent(INDEX_ANNOTATION_CLASS)) {
        IndexAnnotation indexAnnotation = method.getAnnotation(INDEX_ANNOTATION_CLASS);
        addFacetDefinition(facetDefinitions, indexAnnotation);

      } else if (method.isAnnotationPresent(INDEX_ANNOTATIONS_CLASS)) {
        IndexAnnotations indexAnnotations = method.getAnnotation(INDEX_ANNOTATIONS_CLASS);
        IndexAnnotation[] values = indexAnnotations.value();
        for (IndexAnnotation indexAnnotation : values) {
          addFacetDefinition(facetDefinitions, indexAnnotation);
        }
      }
    }

    return facetDefinitions;
  }

  private void addFacetDefinition(List<FacetDefinition> facetDefinitions, IndexAnnotation indexAnnotation) {
    if (indexAnnotation.isFaceted()) {
      FacetDefinition definition = createFacetDefintion(indexAnnotation);
      facetDefinitions.add(definition);
    }
  }

  private FacetDefinition createFacetDefintion(IndexAnnotation indexAnnotation) {
    return new FacetDefinitionBuilder(indexAnnotation.fieldName(), indexAnnotation.title(), indexAnnotation.facetType()).build();
  }
}
