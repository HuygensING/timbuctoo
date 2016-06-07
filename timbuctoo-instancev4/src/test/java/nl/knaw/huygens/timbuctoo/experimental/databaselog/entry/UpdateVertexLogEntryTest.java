package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Set;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static nl.knaw.huygens.timbuctoo.util.PropertyMatcher.likeProperty;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class UpdateVertexLogEntryTest {
  @Test
  public void appendToLogAddsAnUpdateVertexLine() {
    Vertex oldVersion = vertex().withId("id").withProperty("rev", 1).build();
    Vertex vertex = vertex().withId("id").withProperty("rev", 2).build();
    UpdateVertexLogEntry instance = new UpdateVertexLogEntry(vertex, oldVersion);
    DatabaseLog dbLog = mock(DatabaseLog.class);

    instance.appendToLog(dbLog);

    verify(dbLog).updateVertex(vertex);
  }

  @Test
  public void appendToLogAddsNothingToTheLogWhenThePreviousVersionHasTheSameRevision() {
    Vertex oldVersion = vertex().withId("id").withProperty("rev", 2).build();
    Vertex vertex = vertex().withId("id").withProperty("rev", 2).build();
    UpdateVertexLogEntry instance = new UpdateVertexLogEntry(vertex, oldVersion);
    DatabaseLog dbLog = mock(DatabaseLog.class);

    instance.appendToLog(dbLog);

    verifyZeroInteractions(dbLog);
  }

  @Test
  public void appendToLogAddsANewPropertyLineForEachPropertyThatDoesNotExistInThePreviousVersion() {
    Vertex oldVersion = vertex().withId("id").withProperty("oldProp", "oldValue").withProperty("rev", 1).build();
    String newProp = "newProp";
    String newProp2 = "newProp2";
    Vertex vertex = vertex().withId("id")
                            .withProperty(newProp, "value")
                            .withProperty(newProp2, "value")
                            .withProperty("oldProp", "oldValue")
                            .withProperty("rev", 2)
                            .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    UpdateVertexLogEntry instance = new UpdateVertexLogEntry(vertex, oldVersion);

    instance.appendToLog(dbLog);

    verify(dbLog).updateVertex(vertex);
    verify(dbLog).newProperty(argThat(likeProperty().withKey(newProp)));
    verify(dbLog).newProperty(argThat(likeProperty().withKey(newProp2)));
    verify(dbLog).updateProperty(argThat(likeProperty().withKey("rev")));
    verifyNoMoreInteractions(dbLog);
  }

  @Test
  public void appendToLogAddsAnUpdatePropertyLineForEachPropertyThatHasADifferentValueInTheUpdatedVersion() {
    String updatedProp = "updatedProp";
    Vertex oldVersion = vertex().withId("id")
                                .withProperty(updatedProp, "oldValue")
                                .withProperty("oldProp", "oldValue")
                                .withProperty("rev", 1)
                                .build();
    Vertex vertex = vertex().withId("id")
                            .withProperty(updatedProp, "newValue")
                            .withProperty("oldProp", "oldValue")
                            .withProperty("rev", 2)
                            .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    UpdateVertexLogEntry instance = new UpdateVertexLogEntry(vertex, oldVersion);

    instance.appendToLog(dbLog);

    verify(dbLog).updateVertex(vertex);
    verify(dbLog).updateProperty(argThat(likeProperty().withKey(updatedProp)));
    verify(dbLog).updateProperty(argThat(likeProperty().withKey("rev")));
    verifyNoMoreInteractions(dbLog);
  }

  @Test
  public void appendToLogAddsAnDeletePropertyLineForEachPropertyThatDoesNotExistInTheNewVersion() {
    String deletedProp = "deletedProp";
    Vertex oldVersion = vertex().withId("id")
                                .withProperty(deletedProp, "oldValue")
                                .withProperty("oldProp", "oldValue")
                                .withProperty("rev", 1)
                                .build();
    Vertex vertex = vertex().withId("id")
                            .withProperty("oldProp", "oldValue")
                            .withProperty("rev", 2)
                            .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    UpdateVertexLogEntry instance = new UpdateVertexLogEntry(vertex, oldVersion);

    instance.appendToLog(dbLog);

    verify(dbLog).updateVertex(vertex);
    verify(dbLog).deleteProperty(deletedProp);
    verify(dbLog).updateProperty(argThat(likeProperty().withKey("rev")));
    verifyNoMoreInteractions(dbLog);
  }

  @Test
  public void appendToLogIgnoresThePropertiesToIgnore() {
    String deletedPropToIgnore = "deletedPropToIgnore";
    String updatedPropToIgnore = "updatedPropToIgnore";
    String newPropToIgnore = "newPropToIgnore";
    Set<String> propertiesToIgnore = Sets.newHashSet(deletedPropToIgnore, updatedPropToIgnore, newPropToIgnore);
    Vertex oldVersion = vertex().withId("id")
                                .withProperty(deletedPropToIgnore, "oldValue")
                                .withProperty(updatedPropToIgnore, "oldValue")
                                .withProperty("rev", 1)
                                .build();
    Vertex vertex = vertex().withId("id")
                            .withProperty(updatedPropToIgnore, "oldValue")
                            .withProperty(newPropToIgnore, "value")
                            .withProperty("rev", 2)
                            .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    UpdateVertexLogEntry instance = new UpdateVertexLogEntry(vertex, oldVersion, propertiesToIgnore);

    instance.appendToLog(dbLog);

    verify(dbLog).updateVertex(vertex);
    verify(dbLog).updateProperty(argThat(likeProperty().withKey("rev")));
    verifyNoMoreInteractions(dbLog);
  }
}
