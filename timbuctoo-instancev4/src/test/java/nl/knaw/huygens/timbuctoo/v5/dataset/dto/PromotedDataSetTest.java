package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class PromotedDataSetTest {

  @Test
  public void testRegex() throws Exception {
    List<String> ids = Lists.newArrayList(
      //ok
      "foo",
      "foo_bar",

      //not ok
      "foo__bar",
      "foo_bar_",
      "foo_bar",
      "foo_bar ",
      "_foo-bar"
    );
    assertThat(
      ids.stream().filter(x -> x.matches(PromotedDataSet.VALID_ID)).collect(Collectors.toList()),
      contains(
        "foo",
        "foo-bar"
      )
    );
  }

}