package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldConverterMockBuilder.newFieldConverter;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.junit.Test;
import org.neo4j.graphdb.Relationship;

import com.google.common.collect.Lists;

public class RelationConverterTest {

  @Test
  public void addValuesToPropertyContainerCallsAllTheFieldConvertersThatAreNotOnTheIgnoreList() throws ConversionException {
    // setup
    String fieldToIgnore1 = "fieldToIgnore1";
    String fieldToIgnore2 = "fieldToIgnore2";
    List<String> fieldsToIgnore = Lists.newArrayList(fieldToIgnore1, fieldToIgnore2);
    FieldConverter fieldConverterMockToIgnore1 = newFieldConverter().withName(fieldToIgnore1).build();
    FieldConverter fieldConverterMockToIgnore2 = newFieldConverter().withName(fieldToIgnore2).build();
    String name = "someName";
    FieldConverter someFieldConverterMock = newFieldConverter().withName(name).withType(FieldType.ADMINISTRATIVE).build();
    String otherName = "someOtherName";
    FieldConverter someOtherFieldConverterMock = newFieldConverter().withName(otherName).withType(FieldType.REGULAR).build();

    RelationConverter<Relation, Relationship> instance = new RelationConverter<Relation, Relationship>(fieldsToIgnore);
    instance.addFieldConverter(someOtherFieldConverterMock);
    instance.addFieldConverter(someFieldConverterMock);
    instance.addFieldConverter(fieldConverterMockToIgnore2);
    instance.addFieldConverter(fieldConverterMockToIgnore1);

    Relationship relationshipMock = mock(Relationship.class);
    Relation relation = new Relation();

    // action
    instance.addValuesToPropertyContainer(relationshipMock, relation);

    // verify
    verify(someFieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(someOtherFieldConverterMock).setPropertyContainerProperty(relationshipMock, relation);
    verify(fieldConverterMockToIgnore1, never()).setPropertyContainerProperty(relationshipMock, relation);
    verify(fieldConverterMockToIgnore2, never()).setPropertyContainerProperty(relationshipMock, relation);
  }

}
