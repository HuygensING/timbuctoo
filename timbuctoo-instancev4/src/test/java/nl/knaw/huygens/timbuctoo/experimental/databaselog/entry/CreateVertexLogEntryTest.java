package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntry;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static nl.knaw.huygens.timbuctoo.util.PropertyMatcher.likeProperty;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateVertexLogEntryTest {

  private LogEntryFactory logEntryFactory;

  @Before
  public void setUp() throws Exception {
    logEntryFactory = mock(LogEntryFactory.class);
    when(logEntryFactory.createForEdge(any(Edge.class))).thenReturn(mock(EdgeLogEntry.class));
  }

  @Test
  public void appendToLogAddsNewVertexLineForAVertexWithoutPreviousVersions() {
    Vertex vertex = vertex().build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    VertexLogEntry instance = new CreateVertexLogEntry(vertex);

    instance.appendToLog(dbLog);

    verify(dbLog).newVertex(vertex);
  }

  @Test
  public void appendToLogAddsANewPropertyLineForEachNewPropertyAfter() {
    String key1 = "property1";
    String key2 = "property2";
    String key3 = "property3";
    Vertex vertex = vertex()
      .withProperty(key1, "value")
      .withProperty(key2, "value")
      .withProperty(key3, "value")
      .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    CreateVertexLogEntry instance = new CreateVertexLogEntry(vertex);

    instance.appendToLog(dbLog);

    verify(dbLog).newProperty(argThat(likeProperty().withKey(key1)));
    verify(dbLog).newProperty(argThat(likeProperty().withKey(key2)));
    verify(dbLog).newProperty(argThat(likeProperty().withKey(key3)));
  }

  @Test
  public void appendToLogFirstAddsNewVertexThanTheNewProperties() {
    String key1 = "property1";

    Vertex vertex = vertex()
      .withProperty(key1, "value")
      .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    CreateVertexLogEntry vertexLogEntry = new CreateVertexLogEntry(vertex);

    vertexLogEntry.appendToLog(dbLog);

    InOrder inOrder = inOrder(dbLog);
    inOrder.verify(dbLog).newVertex(vertex);
    inOrder.verify(dbLog).newProperty(argThat(likeProperty().withKey(key1)));
  }

  @Test
  public void appendToLogIgnoresThePropertiesToIgnore() {
    DatabaseLog databaseLog = mock(DatabaseLog.class);
    String propToIgnore = "propToIgnore";
    Vertex vertex = vertex().withProperty(propToIgnore, "value").build();
    CreateVertexLogEntry instance = new CreateVertexLogEntry(vertex, Sets.newHashSet(propToIgnore));

    instance.appendToLog(databaseLog);

    verify(databaseLog, never()).newProperty(argThat(likeProperty().withKey(propToIgnore)));
  }

}
