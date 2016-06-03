package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.EdgeMockBuilder.edge;
import static nl.knaw.huygens.timbuctoo.util.PropertyMatcher.likeProperty;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CreateEdgeLogEntryTest {

  @Test
  public void appendToLogLogsANewEdgesHasBeenCreated() {
    Edge edge = edge().build();
    CreateEdgeLogEntry instance = new CreateEdgeLogEntry(edge, 1000L, "id");
    DatabaseLog dbLog = mock(DatabaseLog.class);

    instance.appendToLog(dbLog);

    verify(dbLog).newEdge(edge);
  }

  @Test
  public void appendToLogLogsAPropertyIsAddedForEachPropertyOfTheEdge() {
    Edge edge = edge().withProperty("prop1", "value")
                      .withProperty("prop2", "value")
                      .withProperty("prop3", "value")
                      .build();
    CreateEdgeLogEntry instance = new CreateEdgeLogEntry(edge, 1000L, "id");
    DatabaseLog dbLog = mock(DatabaseLog.class);

    instance.appendToLog(dbLog);

    verify(dbLog).newProperty(argThat(likeProperty().withKey("prop1")));
    verify(dbLog).newProperty(argThat(likeProperty().withKey("prop2")));
    verify(dbLog).newProperty(argThat(likeProperty().withKey("prop3")));
  }

}
