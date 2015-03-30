package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.neo4j.graphdb.Node;

public class PropertyContainerHelperTest {
  @Test
  public void getRevisionPropertyReturnsTheValueOfTheRevisionPropertyAsInt() {
    // setup
    int revision = 1;
    Node node = aNode().withRevision(revision).build();

    // action
    int actualRevision = PropertyContainerHelper.getRevisionProperty(node);

    // verify
    assertThat(actualRevision, is(equalTo(revision)));
  }

  @Test
  public void getRevisionPropertyReturnsZeroIfThePropertyContainerHasNoPropertyRevision() {
    // setup
    Node nodeWithoutRevision = aNode().build();

    // action
    int actualRevision = PropertyContainerHelper.getRevisionProperty(nodeWithoutRevision);

    // verify
    assertThat(actualRevision, is(equalTo(0)));
  }

  @Test
  public void getRevisionPropertyReturnsZeroIfThePropertyContainerIsNull() {
    // setup
    Node nullNode = null;

    // action
    int actualRevision = PropertyContainerHelper.getRevisionProperty(nullNode);

    // verify
    assertThat(actualRevision, is(equalTo(0)));
  }
}
