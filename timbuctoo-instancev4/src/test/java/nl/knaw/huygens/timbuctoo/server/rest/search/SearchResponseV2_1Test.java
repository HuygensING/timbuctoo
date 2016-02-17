package nl.knaw.huygens.timbuctoo.server.rest.search;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SearchResponseV2_1Test {

  @Test
  public void getRowsReturnsTheNumberOfRefsInTheResult() {
    SearchResponseV2_1 instance = new SearchResponseV2_1();

    instance.addRef(new SearchResponseV2_1Ref(null, null, null, null, null));
    instance.addRef(new SearchResponseV2_1Ref(null, null, null, null, null));

    assertThat(instance.getRows(), is(2));
  }

}
