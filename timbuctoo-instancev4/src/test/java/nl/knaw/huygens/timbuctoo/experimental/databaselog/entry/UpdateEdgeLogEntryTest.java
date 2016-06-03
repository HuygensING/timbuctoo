package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.EdgeMockBuilder.edge;
import static nl.knaw.huygens.timbuctoo.util.PropertyMatcher.likeProperty;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class UpdateEdgeLogEntryTest {

  public static final long TIMESTAMP = 1000L;
  public static final String ID = "id";

  @Test
  public void appendToLogLogsTheEdgeIsUpdated() {
    Edge edge = edge().build();
    Edge prevEdge = edge().build();
    DatabaseLog databaseLog = mock(DatabaseLog.class);
    UpdateEdgeLogEntry instance = new UpdateEdgeLogEntry(edge, TIMESTAMP, ID, prevEdge);

    instance.appendToLog(databaseLog);

    verify(databaseLog).updateEdge(edge);
  }

  @Test
  public void appendToLogAddsANewPropertyLineForEachPropertyThatDoesNotExistInThePreviousVersion() {
    Edge prevEdge = edge().withId(ID).withProperty("oldProp", "oldValue").build();
    String newProp = "newProp";
    String newProp2 = "newProp2";
    Edge edge = edge().withId(ID)
                            .withProperty(newProp, "value")
                            .withProperty(newProp2, "value")
                            .withProperty("oldProp", "oldValue")
                            .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    UpdateEdgeLogEntry instance = new UpdateEdgeLogEntry(edge, TIMESTAMP, ID, prevEdge);

    instance.appendToLog(dbLog);

    verify(dbLog).updateEdge(edge);
    verify(dbLog).newProperty(argThat(likeProperty().withKey(newProp)));
    verify(dbLog).newProperty(argThat(likeProperty().withKey(newProp2)));
    verifyNoMoreInteractions(dbLog);
  }

  @Test
  public void appendToLogAddsAnUpdatePropertyLineForEachPropertyThatHasADifferentValueInTheUpdatedVersion() {
    String updatedProp = "updatedProp";
    Edge prevEdge = edge().withId(ID)
                                .withProperty(updatedProp, "oldValue")
                                .withProperty("oldProp", "oldValue")
                                .build();
    Edge edge = edge().withId(ID)
                            .withProperty(updatedProp, "newValue")
                            .withProperty("oldProp", "oldValue")
                            .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    UpdateEdgeLogEntry instance = new UpdateEdgeLogEntry(edge, TIMESTAMP, ID, prevEdge);

    instance.appendToLog(dbLog);

    verify(dbLog).updateEdge(edge);
    verify(dbLog).updateProperty(argThat(likeProperty().withKey(updatedProp)));
    verifyNoMoreInteractions(dbLog);
  }

  @Test
  public void appendToLogAddsAnDeletePropertyLineForEachPropertyThatDoesNotExistInTheNewVersion() {
    String deletedProp = "deletedProp";
    Edge prevEdge = edge().withId(ID)
                            .withProperty(deletedProp, "oldValue")
                            .withProperty("oldProp", "oldValue")
                            .build();
    Edge edge = edge().withId(ID)
                        .withProperty("oldProp", "oldValue")
                        .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    UpdateEdgeLogEntry instance = new UpdateEdgeLogEntry(edge, TIMESTAMP, ID, prevEdge);

    instance.appendToLog(dbLog);

    verify(dbLog).updateEdge(edge);
    verify(dbLog).deleteProperty(deletedProp);
    verifyNoMoreInteractions(dbLog);
  }

}
