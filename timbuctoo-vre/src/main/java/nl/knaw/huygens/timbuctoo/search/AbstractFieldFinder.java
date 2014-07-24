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
import java.util.Set;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.google.common.collect.Sets;

public abstract class AbstractFieldFinder {

  protected static final Class<IndexAnnotations> INDEX_ANNOTATIONS_CLASS = IndexAnnotations.class;
  protected static final Class<IndexAnnotation> INDEX_ANNOTATION_CLASS = IndexAnnotation.class;

  public AbstractFieldFinder() {
    super();
  }

  protected abstract void addField(Set<String> fields, IndexAnnotation indexAnnotation);

  @SuppressWarnings("unchecked")
  public Set<String> findFields(Class<? extends Entity> type) {
    Set<String> fieldList = Sets.newHashSet();

    Method[] methods = type.getMethods();

    for (Method method : methods) {
      if (method.isAnnotationPresent(INDEX_ANNOTATION_CLASS)) {
        IndexAnnotation indexAnnotation = method.getAnnotation(INDEX_ANNOTATION_CLASS);
        addField(fieldList, indexAnnotation);

      } else if (method.isAnnotationPresent(INDEX_ANNOTATIONS_CLASS)) {
        IndexAnnotations indexAnnotations = method.getAnnotation(INDEX_ANNOTATIONS_CLASS);
        IndexAnnotation[] values = indexAnnotations.value();
        for (IndexAnnotation indexAnnotation : values) {
          addField(fieldList, indexAnnotation);
        }
      }
    }

    Class<?> superclass = type.getSuperclass();
    if (!Entity.class.equals(superclass)) {
      fieldList.addAll(findFields((Class<? extends Entity>) type.getSuperclass()));
    }

    return fieldList;
  }

}
