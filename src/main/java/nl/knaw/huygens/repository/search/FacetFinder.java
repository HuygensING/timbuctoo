package nl.knaw.huygens.repository.search;

import java.lang.reflect.Method;
import java.util.Map;

import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.annotations.IndexAnnotations;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.solr.FacetInfo;

import com.google.common.collect.Maps;

class FacetFinder {
  private static final Class<IndexAnnotations> INDEX_ANNOTATIONS_CLASS = IndexAnnotations.class;
  private static final Class<IndexAnnotation> INDEX_ANNOTATION_CLASS = IndexAnnotation.class;

  @SuppressWarnings("unchecked")
  public Map<String, FacetInfo> findFacets(Class<? extends Document> type) {
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
    if (!Document.class.equals(superclass)) {
      facetMap.putAll(findFacets((Class<? extends Document>) type.getSuperclass()));
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
