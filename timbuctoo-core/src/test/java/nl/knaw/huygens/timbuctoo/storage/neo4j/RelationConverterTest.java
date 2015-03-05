package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldConverterMockBuilder.newFieldConverter;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;

public class RelationConverterTest {

  private static final String OTHER_FIELD_CONVERTER = "otherFieldConverter";
  private static final String FIELD_CONVERTER = "fieldConverter";
  private static final String FIELD_TO_IGNORE2 = "fieldToIgnore2";
  private static final String FIELD_TO_IGNORE1 = "fieldToIgnore1";
  private static final ArrayList<String> FIELD_TO_IGNORE = Lists.newArrayList(FIELD_TO_IGNORE1, FIELD_TO_IGNORE2);
  private FieldConverter fieldConverterMockToIgnore1;
  private FieldConverter fieldConverterMockToIgnore2;
  private FieldConverter fieldConverterMock;
  private FieldConverter someOtherFieldConverterMock;
  private Relationship relationshipMock;
  private Relation relation;
  private RelationConverter<Relation, Relationship> instance;

  @Before
  public void setUp() {
    fieldConverterMockToIgnore1 = newFieldConverter().withName(FIELD_TO_IGNORE1).build();
    fieldConverterMockToIgnore2 = newFieldConverter().withName(FIELD_TO_IGNORE2).build();
    fieldConverterMock = newFieldConverter().withName(FIELD_CONVERTER).withType(FieldType.ADMINISTRATIVE).build();
    someOtherFieldConverterMock = newFieldConverter().withName(OTHER_FIELD_CONVERTER).withType(FieldType.REGULAR).build();

    instance = new RelationConverter<Relation, Relationship>(FIELD_TO_IGNORE);
    instance.addFieldConverter(someOtherFieldConverterMock);
    instance.addFieldConverter(fieldConverterMock);
    instance.addFieldConverter(fieldConverterMockToIgnore2);
    instance.addFieldConverter(fieldConverterMockToIgnore1);

    relationshipMock = mock(Relationship.class);
    relation = new Relation();
  }

  @Test
  public void addValuesToPropertyContainerCallsAllTheFieldConvertersThatAreNotOnTheIgnoreList() throws Exception {
    // action
    instance.addValuesToPropertyContainer(relationshipMock, relation);

    // verify
    verify(fieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(someOtherFieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(fieldConverterMockToIgnore1, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(fieldConverterMockToIgnore2, never()).setPropertyContainerProperty(relationshipMock, relation);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToPropertyContainerThrowsAConversionExceptionIfOneOfTheFieldMappersDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(fieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);

    try {
      // action
      instance.addValuesToPropertyContainer(relationshipMock, relation);
    } finally {
      // verify
      verify(fieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    }
  }

  @Test
  public void addValuesToEntityCallsAllTheFieldConvertersThatAreNotOnTheIgnoredList() throws Exception {
    // action
    instance.addValuesToEntity(relation, relationshipMock);

    // verify
    verify(fieldConverterMock).addValueToEntity(relation, relationshipMock);
    verify(someOtherFieldConverterMock).addValueToEntity(relation, relationshipMock);
    verify(fieldConverterMockToIgnore1, never()).addValueToEntity(relation, relationshipMock);
    verify(fieldConverterMockToIgnore2, never()).addValueToEntity(relation, relationshipMock);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionIfOneOfTheFieldMappersDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(fieldConverterMock).addValueToEntity(relation, relationshipMock);

    try {
      // action
      instance.addValuesToEntity(relation, relationshipMock);
    } finally {
      // verify
      verify(fieldConverterMock).addValueToEntity(relation, relationshipMock);;
    }
  }
}
