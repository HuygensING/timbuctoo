package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.PipeFunctionFactory;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopResultFilter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopResultFilterBuilder;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Vertex;

public class TinkerPopResultFilterBuilderTest {
  private PropertyBusinessRules businessRules;
  private PipeFunctionFactory pipeLineFunctionFactory;
  private TinkerPopResultFilterBuilder instance;
  private TimbuctooQuery queryMock;

  @Before
  public void setUp() {
    queryMock = mock(TimbuctooQuery.class);
    businessRules = new PropertyBusinessRules();
    pipeLineFunctionFactory = mock(PipeFunctionFactory.class);

    instance = new TinkerPopResultFilterBuilder(businessRules, pipeLineFunctionFactory);
  }

  @Test
  public void buildCreatesAnIsDistinctFilterForEveryFieldName() throws Exception {
    // action
    TinkerPopResultFilter<Vertex> resultFilter = instance.buildFor(queryMock);

    // verify
    assertThat(resultFilter, is(notNullValue()));

    verify(queryMock).addFilterOptionsToResultFilter(resultFilter);

  }

}
