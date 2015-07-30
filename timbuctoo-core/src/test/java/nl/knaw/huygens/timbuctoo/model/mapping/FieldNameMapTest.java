package nl.knaw.huygens.timbuctoo.model.mapping;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

public class FieldNameMapTest {

  public static final String FROM_1 = "from1";
  public static final String FROM_2 = "from2";
  public static final String FROM_3 = "from3";
  public static final String TO_3 = "to3";
  public static final String TO_2 = "to2";
  public static final String TO_1 = "to1";
  public static final String NON_MAPPED = "nonMapped";
  public static final String VALUE_1 = "value1";
  public static final String VALUE_2 = "value2";
  public static final String VALUE_3 = "value3";

  @Test
  public void remapIteratesThroughAllTheFromFieldsAndRemapsThemIfTheyExistInTheSourceMap(){
    // setup
    FieldNameMap instance = new FieldNameMap();
    instance.put(FROM_1, TO_1);
    instance.put(FROM_2, TO_2);
    instance.put(FROM_3, TO_3);

    Map<String, String> input = Maps.newHashMap();
    input.put(FROM_1, VALUE_1);
    input.put(NON_MAPPED, VALUE_2);
    input.put(FROM_3, VALUE_3);

    // action
    Map<String, String> output = instance.remap(input);

    // verify
    assertThat(output.keySet(), containsInAnyOrder(TO_1, TO_3));
    assertThat(output.get(TO_1), is(VALUE_1));
    assertThat(output.get(TO_3), is(VALUE_3));
  }


}
