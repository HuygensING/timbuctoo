package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubADomainEntity;

public class TinkerPopResultFilterBuilderTest {
  private static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private static final String REGULAR_FIELD = SubADomainEntity.VALUEA3_NAME;
  private static final String ADMINISTRATIVE_FIELD = SubADomainEntity.ID_DB_PROPERTY_NAME;
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
    TinkerPopResultFilter resultFilter = instance.buildFor(queryMock);

    // verify
    assertThat(resultFilter, is(notNullValue()));

    verify(queryMock).addFilterOptionsToResultFilter(resultFilter);

  }

}
