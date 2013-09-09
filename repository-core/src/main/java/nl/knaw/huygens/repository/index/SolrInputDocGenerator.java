package nl.knaw.huygens.repository.index;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.repository.facet.CustomIndexer;
import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.facet.CustomIndexer.NoopIndexer;
import nl.knaw.huygens.repository.model.Document;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SolrInputDocGenerator implements AnnotatedMethodProcessor {
  private SolrInputDocument doc;
  private Document instance;

  public SolrInputDocGenerator(Document instance) {
    this.doc = new SolrInputDocument();
    this.instance = instance;
  }

  public SolrInputDocGenerator(Document instance, SolrInputDocument solrDoc) {
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
  private void indexMethodOnce(SolrInputDocument doc, Document instance, Method m, IndexAnnotation argData) {
    // Determine index field name:
    Class<? extends CustomIndexer> indexClass = argData.customIndexer();
    CustomIndexer indexer;
    String name;
    if (indexClass.equals(NoopIndexer.class)) {
      name = argData.fieldName();
      if (name.length() == 0) {
        name = Utils.getFieldName(m);
      }
      indexer = null;
    } else {
      try {
        indexer = argData.customIndexer().newInstance();
      } catch (Exception e) {
        indexer = null;
        e.printStackTrace();
      }
      name = "";
    }

    boolean canBeEmpty = argData.canBeEmpty();

    // Java reflect is pretty picky:
    try {
      Object value = m.invoke(instance);
      String[] getters = argData.accessors();
      indexObject(doc, name, indexer, value, canBeEmpty, getters);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Evil reflection stuff to deal with getting strings/stuff out of arrays of objects.
   * It will index the result of applying the array of methods on each of the objects.
   */
  private void indexArray(SolrInputDocument doc, String fieldName, CustomIndexer indexer, Object[] array, boolean canBeEmpty, String... methods) {
    try {
      if (!ArrayUtils.isEmpty(array)) {
        for (Object o : array) {
          indexObject(doc, fieldName, indexer, o, canBeEmpty, methods);
        }
      } else if (!canBeEmpty && !StringUtils.isEmpty(fieldName)) {
        doc.addField(fieldName, "(empty)");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void indexObject(SolrInputDocument doc, String fieldName, CustomIndexer indexer, Object o, boolean canBeEmpty, String[] methods) throws IllegalArgumentException, SecurityException,
      IllegalAccessException, InvocationTargetException, NoSuchFieldException {
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
      indexArray(doc, fieldName, indexer, (Object[]) value, canBeEmpty, methodList.toArray(new String[methodList.size()]));
    } else if (List.class.isInstance(value)) {
      @SuppressWarnings("unchecked")
      Object[] values = ((List<Object>) value).toArray();
      indexArray(doc, fieldName, indexer, values, canBeEmpty, methodList.toArray(new String[methodList.size()]));
    } else {
      if (indexer != null) {
        indexer.indexItem(doc, value);
      } else {
        Object transformedValue = transformValue(value, canBeEmpty);
        if (transformedValue != null) {
          doc.addField(fieldName, transformedValue);
        }
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