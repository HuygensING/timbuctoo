package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.RelationshipConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.CompositeRelationshipConverter;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Relationship;

import test.model.projecta.SubARelation;

import com.google.common.collect.Lists;

public class CompositeRelationshipConverterTest {
  private RelationshipConverter<SubARelation> relationshipConverterMock1;
  private RelationshipConverter<? super SubARelation> relationshipConverterMock2;
  private Relationship relationshipMock;
  private SubARelation relationMock;
  private CompositeRelationshipConverter<SubARelation> instance;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    relationshipConverterMock1 = mock(RelationshipConverter.class);
    relationshipConverterMock2 = mock(RelationshipConverter.class);
    relationshipMock = mock(Relationship.class);
    relationMock = new SubARelation();

    ArrayList<RelationshipConverter<? super SubARelation>> converters = Lists.newArrayList();
    converters.add(relationshipConverterMock1);
    converters.add(relationshipConverterMock2);
    instance = new CompositeRelationshipConverter<SubARelation>(converters);
  }

  @Test
  public void addValuesToPropertyContainerDelegatesToTheWrappedNodeConverters() throws Exception {
    // action
    instance.addValuesToPropertyContainer(relationshipMock, relationMock);

    // verify
    verify(relationshipConverterMock1).addValuesToPropertyContainer(relationshipMock, relationMock);
    verify(relationshipConverterMock2).addValuesToPropertyContainer(relationshipMock, relationMock);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToPropertyContainerThrowsAConversionExceptionWhenOneOfTheDelegatesDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(relationshipConverterMock1).addValuesToPropertyContainer(relationshipMock, relationMock);

    try {
      // action
      instance.addValuesToPropertyContainer(relationshipMock, relationMock);
    } finally {
      // verify
      verify(relationshipConverterMock1).addValuesToPropertyContainer(relationshipMock, relationMock);
      verifyZeroInteractions(relationshipConverterMock2);
    }
  }

  @Test
  public void addValuesToEntityDelegatesToTheWrappedNodeConverters() throws ConversionException {
    // action
    instance.addValuesToEntity(relationMock, relationshipMock);

    // verify
    verify(relationshipConverterMock1).addValuesToEntity(relationMock, relationshipMock);
    verify(relationshipConverterMock2).addValuesToEntity(relationMock, relationshipMock);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionWhenOneOfTheDelagatesDoes() throws ConversionException {
    // setup
    doThrow(ConversionException.class).when(relationshipConverterMock1).addValuesToEntity(relationMock, relationshipMock);

    try {
      // action
      instance.addValuesToEntity(relationMock, relationshipMock);
    } finally {
      // verify
      verify(relationshipConverterMock1).addValuesToEntity(relationMock, relationshipMock);
      verifyZeroInteractions(relationshipConverterMock2);
    }
  }

  @Test
  public void updatePropertyContainerDelegatesToTheWrappedNodeConverters() throws Exception {
    // action
    instance.updatePropertyContainer(relationshipMock, relationMock);

    // verify
    verify(relationshipConverterMock1).updatePropertyContainer(relationshipMock, relationMock);
    verify(relationshipConverterMock2).updatePropertyContainer(relationshipMock, relationMock);
  }

  @Test(expected = ConversionException.class)
  public void updatePropertyContainerThrowsAConversionExceptionWhenOneOfTheDelagatesDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(relationshipConverterMock1).updatePropertyContainer(relationshipMock, relationMock);
    try {
      // action
      instance.updatePropertyContainer(relationshipMock, relationMock);
    } finally {
      // verify
      verify(relationshipConverterMock1).updatePropertyContainer(relationshipMock, relationMock);
      verifyZeroInteractions(relationshipConverterMock2);
    }

  }

  @Test
  public void updateModifiedAndRevDelegatesToTheWrappedNodeConverters() throws Exception {
    // action
    instance.updateModifiedAndRev(relationshipMock, relationMock);

    // verify
    verify(relationshipConverterMock1).updateModifiedAndRev(relationshipMock, relationMock);
    verify(relationshipConverterMock2).updateModifiedAndRev(relationshipMock, relationMock);
  }

  @Test(expected = ConversionException.class)
  public void updateModifiedAndRevThrowsAConversionExceptionWhenOneOfTheDelagatesDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(relationshipConverterMock1).updateModifiedAndRev(relationshipMock, relationMock);
    try {
      // action
      instance.updateModifiedAndRev(relationshipMock, relationMock);
    } finally {
      // verify
      verify(relationshipConverterMock1).updateModifiedAndRev(relationshipMock, relationMock);
      verifyZeroInteractions(relationshipConverterMock2);
    }
  }
}
