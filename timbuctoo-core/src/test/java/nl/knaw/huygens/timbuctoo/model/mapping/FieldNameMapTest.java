package nl.knaw.huygens.timbuctoo.model.mapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.List;
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
  public static final String SIMPLE_VALUE = "value1";
  public static final String OTHER_SIMPLE_VALUE = "value2";
  public static final String LIST_ENTRY_1 = "listEntry1";
  public static final String LIST_ENTRY_2 = "listEntry2";
  public static final List<String> LIST_VALUE = Lists.newArrayList(LIST_ENTRY_1, LIST_ENTRY_2);

  @Test
  public void remapIteratesThroughAllTheFromFieldsAndRemapsThemIfTheyExistInTheSourceMap(){
    // setup
    FieldNameMap instance = new FieldNameMap();
    instance.put(FROM_1, TO_1);
    instance.put(FROM_2, TO_2);
    instance.put(FROM_3, TO_3);

    Map<String, Object> input = Maps.newHashMap();
    input.put(FROM_1, SIMPLE_VALUE);
    input.put(NON_MAPPED, OTHER_SIMPLE_VALUE);
    input.put(FROM_3, LIST_VALUE);

    // action
    Map<String, Object> output = instance.remap(input);

    // verify
    assertThat(output.keySet(), containsInAnyOrder(TO_1, TO_3));
    assertThat(output.get(TO_1), is(SIMPLE_VALUE));
    assertThat(output.get(TO_3), is(LIST_VALUE));
  }


}
