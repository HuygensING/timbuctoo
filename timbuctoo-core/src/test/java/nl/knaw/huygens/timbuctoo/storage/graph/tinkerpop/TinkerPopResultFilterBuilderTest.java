package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubADomainEntity;

import com.google.common.collect.Lists;

public class TinkerPopResultFilterBuilderTest {
  private static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private static final String REGULAR_FIELD = SubADomainEntity.VALUEA3_NAME;
  private static final String ADMINISTRATIVE_FIELD = SubADomainEntity.ID_DB_PROPERTY_NAME;
  private PropertyBusinessRules businessRules;
  private PipeFunctionFactory pipeLineFunctionFactory;
  private TinkerPopResultFilterBuilder instance;

  @Before
  public void setUp() {
    businessRules = new PropertyBusinessRules();
    pipeLineFunctionFactory = mock(PipeFunctionFactory.class);

    instance = new TinkerPopResultFilterBuilder(businessRules, pipeLineFunctionFactory);
  }

  @Test
  public void buildCreatesAnIsDistinctFilterForEveryFieldName() throws Exception {
    // setup
    String regularFieldPropertyName = getPropertyName(TYPE, REGULAR_FIELD);
    ArrayList<String> distinctValues = Lists.newArrayList(//
        REGULAR_FIELD, //
        ADMINISTRATIVE_FIELD);
    instance.setHasDistinctValues(distinctValues);

    // action
    TinkerPopResultFilter resultFilter = instance.buildFor(TYPE);

    // verify
    assertThat(resultFilter, is(notNullValue()));

    verify(pipeLineFunctionFactory).forDistinctProperty(ADMINISTRATIVE_FIELD);
    verify(pipeLineFunctionFactory).forDistinctProperty(regularFieldPropertyName);

  }

  private String getPropertyName(Class<? extends Entity> type, String name) throws Exception {
    Field field = type.getDeclaredField(name);

    String fieldName = businessRules.getFieldName(type, field);

    return businessRules.getFieldType(TYPE, field).propertyName(TYPE, fieldName);

  }

  @Test(expected = NoSuchFieldException.class)
  public void buildThrowsANoSuchFieldExceptionIfAFieldDoesNotExistForAName() {
    // setup
    String unknownFieldName = "unknownField";
    ArrayList<String> distinctValues = Lists.newArrayList(unknownFieldName);
    instance.setHasDistinctValues(distinctValues);

    // action
    instance.buildFor(TYPE);
  }
}
