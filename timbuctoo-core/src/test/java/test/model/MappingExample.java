package test.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

@IDPrefix("MEXA")
public class MappingExample extends DomainEntity {
  public static final String FIELD_WITHOUT_GETTER = "fieldWithoutGetter";
  public static final String FIELD_NAME_OF_GETTER_WITH_ANNOTATION = "fieldNameOfGetterWithAnnotation";
  public static final String FIRST_NON_SORTABLE = "firstNonSortable";
  public static final String CLIENT_FIELD_FIRST_NON_SORTABLE = "clientFieldFirstNonSortable";
  public static final String VIRTUAL_SUPER_PROPERTY = "virtualSuperProperty";

  @JsonProperty(FIELD_WITHOUT_GETTER)
  private Object fieldWithoutGetter;

  public static final String INDEX_AND_CLIENT_INDEX_NAME = "execute";
  public static final String INDEX_AND_CLIENT_CLIENT_NAME = "client";
  @JsonProperty(INDEX_AND_CLIENT_CLIENT_NAME)
  private Object indexAndClient;

  private Object fieldWithGetterWithoutIndexAnnotation;

  private Object fieldWithGetterWithJsonPropertyAnnotation;

  private Object fieldWithGetterWithoutAnnotations;

  private Object fieldWithSortableIndexAnnotation;

  @JsonProperty(CLIENT_FIELD_FIRST_NON_SORTABLE)
  private Object fieldWithIndexAnnotations;

  @Override
  public String getIdentificationName() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @IndexAnnotation(fieldName = INDEX_AND_CLIENT_INDEX_NAME)
  public Object getIndexAndClient() {
    return indexAndClient;
  }

  public Object getFieldWithGetterWithoutIndexAnnotation() {
    return fieldWithGetterWithoutIndexAnnotation;
  }

  @JsonProperty(FIELD_NAME_OF_GETTER_WITH_ANNOTATION)
  public Object getFieldWithGetterWithJsonPropertyAnnotation() {
    return fieldWithGetterWithJsonPropertyAnnotation;
  }

  public Object getFieldWithGetterWithoutAnnotations() {
    return fieldWithGetterWithoutAnnotations;
  }

  @IndexAnnotation(fieldName = "fieldName", isSortable = true)
  public Object getFieldWithSortableIndexAnnotation() {
    return fieldWithSortableIndexAnnotation;
  }

  @IndexAnnotations({//
    @IndexAnnotation(fieldName = "fieldName", isSortable = true), //
    @IndexAnnotation(fieldName = FIRST_NON_SORTABLE), //
    @IndexAnnotation(fieldName = "other")})
  public Object getFieldWithIndexAnnotations() {
    return fieldWithIndexAnnotations;
  }


  @IndexAnnotation(fieldName = VIRTUAL_SUPER_PROPERTY)
  public String getVirtualSuperProperty() {
    return null;
  }
}
