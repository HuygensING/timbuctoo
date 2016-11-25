package nl.knaw.huygens.timbuctoo.server.healthchecks;

import nl.knaw.huygens.timbuctoo.util.TestGraphBuilder;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DatabaseValidatorTest {

  @Test
  public void checkRunsAllTheDatabaseChecksForEachVertex() {
    String id1 = "id1";
    String id2 = "id2";
    Graph graph = makeGraph(id1, id2);
    DatabaseCheck databaseCheck1 = mock(DatabaseCheck.class);
    DatabaseCheck databaseCheck2 = mock(DatabaseCheck.class);

    DatabaseValidator instance =
      new DatabaseValidator(databaseCheck1, databaseCheck2);

    instance.check(graph);

    verify(databaseCheck1).check(argThat(likeVertex().withTimId(id1)));
    verify(databaseCheck1).check(argThat(likeVertex().withTimId(id2)));
    verify(databaseCheck2).check(argThat(likeVertex().withTimId(id1)));
    verify(databaseCheck2).check(argThat(likeVertex().withTimId(id2)));
  }

  @Test
  public void checkReturnsTheValidationResult() {
    Graph graph = makeGraph("id1", "id2");
    DatabaseValidator instance = new DatabaseValidator();

    ValidationResult result = instance.check(graph);

    assertThat(result, is(notNullValue()));
  }

  @Test
  public void checkReturnsANonValidValidationResultWhenAtLeastOneOfTheChecksFails() {
    Graph graph = makeGraph("id1", "id2");
    DatabaseValidator instance = new DatabaseValidator(nonValidDatabaseCheck());

    ValidationResult validationResult = instance.check(graph);

    assertThat(validationResult.isValid(), is(false));
  }

  @Test
  public void checkReturnsAValidValidationResultWhenAllOfTheChecksSucceed() {
    Graph graph = makeGraph("id", "id2");
    DatabaseValidator instance = new DatabaseValidator(validDatabaseCheck());

    ValidationResult result = instance.check(graph);

    assertThat(result.isValid(), is(true));
  }

  private DatabaseCheck validDatabaseCheck() {
    return createDatabaseCheck(true);
  }

  private DatabaseCheck nonValidDatabaseCheck() {
    return createDatabaseCheck(false);
  }

  private DatabaseCheck createDatabaseCheck(final boolean isValid) {
    DatabaseCheck databaseCheck = mock(DatabaseCheck.class);
    given(databaseCheck.check(any(Vertex.class))).willReturn(new ValidationResult() {
      @Override
      public boolean isValid() {
        return isValid;
      }

      @Override
      public String getMessage() {
        throw new UnsupportedOperationException("Not implemented yet");
      }
    });
    return databaseCheck;
  }

  private Graph makeGraph(String... ids) {
    TestGraphBuilder graphBuilder = newGraph();
    for (String id : ids) {
      graphBuilder.withVertex(vertex -> vertex.withTimId(id));
    }
    return graphBuilder.build();
  }
}
