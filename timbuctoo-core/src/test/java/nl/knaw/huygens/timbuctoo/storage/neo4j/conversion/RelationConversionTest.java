package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.DELETED;
import static nl.knaw.huygens.timbuctoo.model.DomainEntity.PID;
import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID;
import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_TYPE;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_TYPE;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipMockBuilder.aRelationship;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubARelationBuilder.aRelation;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.RelationshipConverter;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Relationship;

import test.model.projecta.SubARelation;

/**
 * An integration test to configure which fields are not serialized. 
 */
public class RelationConversionTest {

  private static final String A_STRING = "aString";
  private RelationshipConverter<SubARelation> converter;

  @Before
  public void setup() throws ModelException {
    // setup
    TypeRegistry typeRegistry = TypeRegistry.getInstance();
    typeRegistry.init("timbuctoo.model test.model.projecta");

    PropertyContainerConverterFactory converterFactory = new PropertyContainerConverterFactory(typeRegistry);
    converter = converterFactory.createForRelation(SubARelation.class);
  }

  @Test
  public void sourceTypeSourceIdTargetTypeAndTargetIdAreNotAddedToTheRelationship() throws Exception {
    // setup
    Relationship relationshipMock = aRelationship().build();

    SubARelation relation = aRelation().withId(A_STRING).withAPID() //
        .withSourceId(A_STRING).withSourceType(A_STRING)//
        .withTargetType(A_STRING).withTargetId(A_STRING)//
        .build();

    // action
    converter.addValuesToPropertyContainer(relationshipMock, relation);

    // verify
    verify(relationshipMock).setProperty(ID_PROPERTY_NAME, A_STRING);
    verify(relationshipMock).setProperty(argThat(equalTo(PID)), anyString());
    verify(relationshipMock).setProperty(argThat(equalTo(DELETED)), anyBoolean());
    verify(relationshipMock).setProperty(argThat(endsWith("accepted")), anyBoolean());
    verify(relationshipMock).setProperty(argThat(equalTo(REVISION_PROPERTY_NAME)), anyInt());

    verifyTheFieldsAnnotatedWithDBIgnoreAreIgnored(relationshipMock);

    verifyNoMoreInteractions(relationshipMock);
  }

  private void verifyTheFieldsAnnotatedWithDBIgnoreAreIgnored(Relationship relationship) {
    verify(relationship, never()).setProperty(argThat(equalTo(SOURCE_TYPE)), anyString());
    verify(relationship, never()).setProperty(argThat(equalTo(SOURCE_ID)), anyString());
    verify(relationship, never()).setProperty(argThat(equalTo(TARGET_TYPE)), anyString());
    verify(relationship, never()).setProperty(argThat(equalTo(TARGET_ID)), anyString());
  }
}
