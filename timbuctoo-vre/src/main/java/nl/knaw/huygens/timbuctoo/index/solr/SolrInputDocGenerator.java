package nl.knaw.huygens.timbuctoo.index.solr;

/*
 * #%L
 * Timbuctoo VRE
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

import static nl.knaw.huygens.timbuctoo.search.FacetFinder.LOWER_LIMIT_POST_FIX;
import static nl.knaw.huygens.timbuctoo.search.FacetFinder.UPPER_LIMIT_POST_FIX;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.FacetType;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.index.AnnotatedMethodProcessor;
import nl.knaw.huygens.timbuctoo.index.Utils;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Range;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SolrInputDocGenerator implements AnnotatedMethodProcessor {

  private SolrInputDocument doc;
  private Entity instance;

  public SolrInputDocGenerator(Entity instance) {
    this.doc = new SolrInputDocument();
    this.instance = instance;
  }

  public SolrInputDocGenerator(Entity instance, SolrInputDocument solrDoc) {
    this.doc = solrDoc;
    this.instance = instance;
  }

  @Override
  public void process(Method m, IndexAnnotation annotation) {
    indexMethodOnce(doc, instance, m, annotation);
  }

  public SolrInputDocument getResult() {
    Collection<String> fieldNames = doc.getFieldNames();
    Collection<Object> values = null;
    Set<Object> nonDuplicateValues = null;

    for (String fieldName : fieldNames) {
      values = doc.getFieldValues(fieldName);

      nonDuplicateValues = Sets.newHashSet(values);

      if (values.size() > nonDuplicateValues.size()) {
        doc.setField(fieldName, nonDuplicateValues);
      }

      if (nonDuplicateValues.size() >= 2 && nonDuplicateValues.contains("(empty)")) {
        nonDuplicateValues.remove("(empty)");
        doc.setField(fieldName, nonDuplicateValues);
      }

      if (fieldName.startsWith("dynamic_sort_")) {
        Object o = values.iterator().next();
        doc.setField(fieldName, o);
      }
    }

    return doc;
  }

  /**
   * Index this part of the item.
   */
  private void indexMethodOnce(SolrInputDocument doc, Entity instance, Method m, IndexAnnotation indexAnnotation) {
    // Determine execute field name:
    String name = indexAnnotation.fieldName();
    if (name.length() == 0) {
      name = Utils.getFieldName(m);
    }

    boolean canBeEmpty = indexAnnotation.canBeEmpty();

    // Java reflect is pretty picky:
    try {
      Object value = m.invoke(instance);
      String[] getters = indexAnnotation.accessors();
      FacetType facetType = indexAnnotation.facetType();
      if (facetType == FacetType.RANGE) {
        indexRange(doc, name, value, canBeEmpty, getters);
      } else {
        indexObject(doc, name, value, canBeEmpty, getters);
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /*
   *  FIXME awful hack to execute range facets. It shares logic with nl.knaw.huygens.timbuctoo.search.FacetFinder.
   *  See issue #2642
   */
  private void indexRange(SolrInputDocument doc, String name, Object value, boolean canBeEmpty, String[] getters) {
    if (!(value instanceof Range)) {
      return;
    }

    Range range = (Range) value;

    if (!range.isValid()) {
      return;
    }

    doc.addField(String.format("%s_%s", name, LOWER_LIMIT_POST_FIX), range.getLowerLimit());
    doc.addField(String.format("%s_%s", name, UPPER_LIMIT_POST_FIX), range.getUpperLimit());
  }

  /**
   * Evil reflection stuff to deal with getting strings/stuff out of arrays of objects.
   * It will execute the result of applying the array of methods on each of the objects.
   */
  private void indexArray(SolrInputDocument doc, String fieldName, Object[] array, boolean canBeEmpty, String... methods) {
    try {
      if (!ArrayUtils.isEmpty(array)) {
        for (Object o : array) {
          indexObject(doc, fieldName, o, canBeEmpty, methods);
        }
      } else if (!canBeEmpty && !StringUtils.isEmpty(fieldName)) {
        doc.addField(fieldName, "(empty)");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void indexObject(SolrInputDocument doc, String fieldName, Object o, boolean canBeEmpty, String[] methods) throws IllegalArgumentException, SecurityException, IllegalAccessException,
      InvocationTargetException, NoSuchFieldException {
    Object value = o;
    List<String> methodList = Lists.newArrayList(methods);
    // Pop off accessors (fields or methods) until:
    // -- there's nothing left; or
    // -- the result is null; or
    // -- the result is an array or list.
    while (!methodList.isEmpty() && value != null && !value.getClass().isArray() && !List.class.isInstance(value)) {
      String method = methodList.remove(0);
      try {
        value = value.getClass().getMethod(method).invoke(value);
      } catch (NoSuchMethodException ex) {
        value = value.getClass().getField(method).get(value);
      }
    }
    // If this is an array or list, process as such:
    if (value != null && value.getClass().isArray()) {
      indexArray(doc, fieldName, (Object[]) value, canBeEmpty, methodList.toArray(new String[methodList.size()]));
    } else if (List.class.isInstance(value)) {
      @SuppressWarnings("unchecked")
      Object[] values = ((List<Object>) value).toArray();
      indexArray(doc, fieldName, values, canBeEmpty, methodList.toArray(new String[methodList.size()]));
    } else {
      Object transformedValue = transformValue(value, canBeEmpty);
      if (transformedValue != null) {
        doc.addField(fieldName, transformedValue);
      }
    }
  }

  private Object transformValue(Object value, boolean canBeEmpty) {
    try {
      if (value == null && !canBeEmpty) {
        return "(empty)";
      }
      if (!(value instanceof String)) {
        if (value instanceof Number && canBeEmpty && value.equals(0)) {
          return null;
        }
        return value;
      }
      String strValue = (String) value;
      if (strValue.isEmpty() && !canBeEmpty) {
        return "(empty)";
      }
      return strValue;
    } catch (Exception ex) {
      return value;
    }
  }
}
