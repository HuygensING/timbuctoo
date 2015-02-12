package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.neo4j.graphdb.Node;

public class EntityWrapperTest {
  @Test
  public void addValuesToNodeLetsTheFieldWrappersAddTheirValuesToTheNode() throws Exception {
    FieldWrapper fieldWrapperMock1 = mock(FieldWrapper.class);
    FieldWrapper fieldWrapperMock2 = mock(FieldWrapper.class);

    EntityWrapper instance = new EntityWrapper();
    instance.addFieldWrapper(fieldWrapperMock1);
    instance.addFieldWrapper(fieldWrapperMock2);

    Node nodeMock = mock(Node.class);

    // action
    instance.addValuesToNode(nodeMock);

    // verify
    verify(fieldWrapperMock1).addValueToNode(nodeMock);
    verify(fieldWrapperMock2).addValueToNode(nodeMock);
  }
}
