package nl.knaw.huygens.timbuctoo.search;

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
