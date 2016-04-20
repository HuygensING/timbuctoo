package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.TestGraphBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.time.Clock;
import java.util.ArrayList;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DatabaseValidatorTest {

  @Test
  public void checkRunsAllTheDatabaseChecksForEachVertex() {
    String id1 = "id1";
    String id2 = "id2";
    GraphWrapper graphWrapper = createGraphWrapper(id1, id2);
    DatabaseCheck databaseCheck1 = mock(DatabaseCheck.class);
    DatabaseCheck databaseCheck2 = mock(DatabaseCheck.class);
    ArrayList<DatabaseCheck> databaseChecks = Lists.newArrayList(databaseCheck1, databaseCheck2);
    DatabaseValidator instance =
      new DatabaseValidator(graphWrapper, 1, Clock.systemUTC(), databaseChecks);

    instance.check();

    verify(databaseCheck1).check(argThat(likeVertex().withTimId(id1)));
    verify(databaseCheck1).check(argThat(likeVertex().withTimId(id2)));
    verify(databaseCheck2).check(argThat(likeVertex().withTimId(id1)));
    verify(databaseCheck2).check(argThat(likeVertex().withTimId(id2)));
  }

  @Test
  public void checkReturnsTheValidationResult() {
    GraphWrapper graphWrapper = createGraphWrapper("id1", "id2");
    DatabaseValidator instance = new DatabaseValidator(graphWrapper, 1, Clock.systemUTC(), Lists.newArrayList());

    ValidationResult result = instance.check();

    assertThat(result, is(notNullValue()));
  }

  @Test
  public void checkReturnsANonValidValidationResultWhenAtLeastOneOfTheChecksFails() {
    GraphWrapper graphWrapper = createGraphWrapper("id1", "id2");
    DatabaseCheck databaseCheck = nonValidDatabaseCheck();
    DatabaseValidator instance =
      new DatabaseValidator(graphWrapper, 1, Clock.systemUTC(), Lists.newArrayList(databaseCheck));

    ValidationResult validationResult = instance.check();

    assertThat(validationResult.isValid(), is(false));
  }

  @Test
  public void checkReturnsAValidValidationResultWhenAllOfTheChecksSucceed() {
    GraphWrapper graphWrapper = createGraphWrapper("id", "id2");
    DatabaseCheck databaseCheck = validDatabaseCheck();

    DatabaseValidator instance =
      new DatabaseValidator(graphWrapper, 1, Clock.systemUTC(), Lists.newArrayList(databaseCheck));

    ValidationResult result = instance.check();

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

  private GraphWrapper createGraphWrapper(String... ids) {
    TestGraphBuilder graphBuilder = newGraph();
    for (String id : ids) {
      graphBuilder.withVertex(vertex -> vertex.withTimId(id));
    }
    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    given(graphWrapper.getGraph()).willReturn(graphBuilder.build());
    return graphWrapper;
  }
}
