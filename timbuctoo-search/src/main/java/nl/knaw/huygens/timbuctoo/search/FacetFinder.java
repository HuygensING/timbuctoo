package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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

import java.lang.reflect.Method;
import java.util.Map;

import nl.knaw.huygens.solr.FacetInfo;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Maps;

class FacetFinder {

  private static final Class<IndexAnnotations> INDEX_ANNOTATIONS_CLASS = IndexAnnotations.class;
  private static final Class<IndexAnnotation> INDEX_ANNOTATION_CLASS = IndexAnnotation.class;

  @SuppressWarnings("unchecked")
  public Map<String, FacetInfo> findFacets(Class<? extends Entity> type) {
    Map<String, FacetInfo> facetMap = Maps.newHashMap();

    Method[] methods = type.getMethods();

    for (Method method : methods) {
      if (method.isAnnotationPresent(INDEX_ANNOTATION_CLASS)) {
        IndexAnnotation indexAnnotation = method.getAnnotation(INDEX_ANNOTATION_CLASS);
        addFacet(facetMap, indexAnnotation);

      } else if (method.isAnnotationPresent(INDEX_ANNOTATIONS_CLASS)) {
        IndexAnnotations indexAnnotations = method.getAnnotation(INDEX_ANNOTATIONS_CLASS);
        IndexAnnotation[] values = indexAnnotations.value();
        for (IndexAnnotation indexAnnotation : values) {
          addFacet(facetMap, indexAnnotation);
        }
      }
    }

    Class<?> superclass = type.getSuperclass();
    if (!Entity.class.equals(superclass)) {
      facetMap.putAll(findFacets((Class<? extends Entity>) type.getSuperclass()));
    }

    return facetMap;
  }

  private void addFacet(Map<String, FacetInfo> facets, IndexAnnotation indexAnnotation) {
    if (indexAnnotation.isFaceted()) {
      FacetInfo info = createFacetInfo(indexAnnotation);
      facets.put(info.getName(), info);
    }
  }

  private FacetInfo createFacetInfo(IndexAnnotation indexAnnotation) {
    return new FacetInfo().setName(indexAnnotation.fieldName()).setTitle(indexAnnotation.title()).setType(indexAnnotation.facetType());
  }
}
