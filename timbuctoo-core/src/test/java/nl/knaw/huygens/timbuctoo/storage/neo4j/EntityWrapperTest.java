package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import test.model.TestSystemEntityWrapper;

public class EntityWrapperTest {
  @Test
  public void addValuesToNodeLetsTheFieldWrappersAddTheirValuesToTheNode() throws Exception {
    FieldWrapper fieldWrapperMock1 = mock(FieldWrapper.class);
    FieldWrapper fieldWrapperMock2 = mock(FieldWrapper.class);
    String typeName = "typeName";

    NameCreator nameCreatorMock = mock(NameCreator.class);
    when(nameCreatorMock.typeName(TestSystemEntityWrapper.class)).thenReturn(typeName);

    EntityWrapper instance = new EntityWrapper();
    instance.addFieldWrapper(fieldWrapperMock1);
    instance.addFieldWrapper(fieldWrapperMock2);
    instance.setNameCreator(nameCreatorMock);
    instance.setEntity(new TestSystemEntityWrapper());

    Node nodeMock = mock(Node.class);

    // action
    instance.addValuesToNode(nodeMock);

    // verify
    verify(nodeMock).addLabel(DynamicLabel.label(typeName));
    verify(fieldWrapperMock1).addValueToNode(nodeMock);
    verify(fieldWrapperMock2).addValueToNode(nodeMock);
  }
}
