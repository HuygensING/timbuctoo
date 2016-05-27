package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.mockito.InOrder;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static nl.knaw.huygens.timbuctoo.util.VertexPropertyMatcher.likeVertexProperty;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CreateVertexLogEntryTest {
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
    VertexLogEntry instance = new CreateVertexLogEntry(vertex);

    instance.appendToLog(dbLog);

    verify(dbLog).newProperty(argThat(likeVertexProperty().withKey(key1)));
    verify(dbLog).newProperty(argThat(likeVertexProperty().withKey(key2)));
    verify(dbLog).newProperty(argThat(likeVertexProperty().withKey(key3)));
  }

  @Test
  public void appendLogFirstAddsNewVertexThanTheNewProperties() {
    String key1 = "property1";

    Vertex vertex = vertex()
      .withProperty(key1, "value")
      .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    VertexLogEntry vertexLogEntry = new CreateVertexLogEntry(vertex);

    vertexLogEntry.appendToLog(dbLog);

    InOrder inOrder = inOrder(dbLog);
    inOrder.verify(dbLog).newVertex(vertex);
    inOrder.verify(dbLog).newProperty(argThat(likeVertexProperty().withKey(key1)));
  }

}
