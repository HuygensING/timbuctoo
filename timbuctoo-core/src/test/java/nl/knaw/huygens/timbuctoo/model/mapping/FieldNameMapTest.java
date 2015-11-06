package nl.knaw.huygens.timbuctoo.model.mapping;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FieldNameMapTest {

  public static final String FROM_1 = "from1";
  public static final String FROM_2 = "from2";
  public static final String TO_2 = "to2";
  public static final String TO_1 = "to1";
  public static final String NON_MAPPED = "nonMapped";
  public static final String SIMPLE_VALUE = "value1";
  public static final String OTHER_SIMPLE_VALUE = "value2";

  @Test
  public void remapIteratesThroughAllTheFromFieldsAndRemapsThemIfTheyExistInTheSourceMap() {
    // setup
    DomainEntity entity = mock(DomainEntity.class);
    FieldNameMap instance = new FieldNameMap(entity);
    instance.put(FROM_1, TO_1);
    instance.put(FROM_2, TO_2);

    Map<String, String> input = Maps.newHashMap();
    input.put(FROM_1, SIMPLE_VALUE);
    input.put(NON_MAPPED, OTHER_SIMPLE_VALUE);

    // action
    Map<String, String> output = instance.remap(input);

    // verify
    ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
    verify(entity).createRelSearchRep(mapArgumentCaptor.capture());

    Map<String, Object> capturedMap = mapArgumentCaptor.getValue();
    assertThat(capturedMap.keySet(), containsInAnyOrder(TO_1));
    assertThat(capturedMap.get(TO_1), is(SIMPLE_VALUE));
  }


}
