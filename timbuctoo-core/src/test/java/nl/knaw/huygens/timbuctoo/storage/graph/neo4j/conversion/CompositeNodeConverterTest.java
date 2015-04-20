package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.NodeConverter;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.CompositeNodeConverter;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import test.model.projecta.SubADomainEntity;

import com.google.common.collect.Lists;

public class CompositeNodeConverterTest {
  private NodeConverter<SubADomainEntity> nodeConverterMock1;
  private NodeConverter<? super SubADomainEntity> nodeConverterMock2;
  private Node nodeMock;
  private SubADomainEntity entity;
  private CompositeNodeConverter<SubADomainEntity> instance;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    nodeConverterMock1 = mock(NodeConverter.class);
    nodeConverterMock2 = mock(NodeConverter.class);
    nodeMock = mock(Node.class);
    entity = new SubADomainEntity();

    ArrayList<NodeConverter<? super SubADomainEntity>> converters = Lists.newArrayList();
    converters.add(nodeConverterMock1);
    converters.add(nodeConverterMock2);
    instance = new CompositeNodeConverter<SubADomainEntity>(converters);
  }

  @Test
  public void addValuesToPropertyContainerDelegatesToTheWrappedNodeConverters() throws Exception {
    // action
    instance.addValuesToPropertyContainer(nodeMock, entity);

    // verify
    verify(nodeConverterMock1).addValuesToPropertyContainer(nodeMock, entity);
    verify(nodeConverterMock2).addValuesToPropertyContainer(nodeMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToPropertyContainerThrowsAConversionExceptionWhenOneOfTheDelegatesDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(nodeConverterMock1).addValuesToPropertyContainer(nodeMock, entity);

    try {
      // action
      instance.addValuesToPropertyContainer(nodeMock, entity);
    } finally {
      // verify
      verify(nodeConverterMock1).addValuesToPropertyContainer(nodeMock, entity);
      verifyZeroInteractions(nodeConverterMock2);
    }
  }

  @Test
  public void addValuesToEntityDelegatesToTheWrappedNodeConverters() throws ConversionException {
    // action
    instance.addValuesToEntity(entity, nodeMock);

    // verify
    verify(nodeConverterMock1).addValuesToEntity(entity, nodeMock);
    verify(nodeConverterMock2).addValuesToEntity(entity, nodeMock);
  }

  @Test(expected = ConversionException.class)
  public void addValuesToEntityThrowsAConversionExceptionWhenOneOfTheDelagatesDoes() throws ConversionException {
    // setup
    doThrow(ConversionException.class).when(nodeConverterMock1).addValuesToEntity(entity, nodeMock);

    try {
      // action
      instance.addValuesToEntity(entity, nodeMock);
    } finally {
      // verify
      verify(nodeConverterMock1).addValuesToEntity(entity, nodeMock);
      verifyZeroInteractions(nodeConverterMock2);
    }
  }

  @Test
  public void updatePropertyContainerDelegatesToTheWrappedNodeConverters() throws Exception {
    // action
    instance.updatePropertyContainer(nodeMock, entity);

    // verify
    verify(nodeConverterMock1).updatePropertyContainer(nodeMock, entity);
    verify(nodeConverterMock2).updatePropertyContainer(nodeMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void updatePropertyContainerThrowsAConversionExceptionWhenOneOfTheDelagatesDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(nodeConverterMock1).updatePropertyContainer(nodeMock, entity);
    try {
      // action
      instance.updatePropertyContainer(nodeMock, entity);
    } finally {
      // verify
      verify(nodeConverterMock1).updatePropertyContainer(nodeMock, entity);
      verifyZeroInteractions(nodeConverterMock2);
    }

  }

  @Test
  public void updateModifiedAndRevDelegatesToTheWrappedNodeConverters() throws Exception {
    // action
    instance.updateModifiedAndRev(nodeMock, entity);

    // verify
    verify(nodeConverterMock1).updateModifiedAndRev(nodeMock, entity);
    verify(nodeConverterMock2).updateModifiedAndRev(nodeMock, entity);
  }

  @Test(expected = ConversionException.class)
  public void updateModifiedAndRevThrowsAConversionExceptionWhenOneOfTheDelagatesDoes() throws Exception {
    // setup
    doThrow(ConversionException.class).when(nodeConverterMock1).updateModifiedAndRev(nodeMock, entity);
    try {
      // action
      instance.updateModifiedAndRev(nodeMock, entity);
    } finally {
      // verify
      verify(nodeConverterMock1).updateModifiedAndRev(nodeMock, entity);
      verifyZeroInteractions(nodeConverterMock2);
    }
  }

}
