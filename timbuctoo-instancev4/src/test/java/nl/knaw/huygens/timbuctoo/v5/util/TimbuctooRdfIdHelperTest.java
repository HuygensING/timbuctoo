package nl.knaw.huygens.timbuctoo.v5.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TimbuctooRdfIdHelperTest {

  private TimbuctooRdfIdHelper instance;

  @Before
  public void setUp() {
    instance = new TimbuctooRdfIdHelper("http://timbuctoo.huygens.knaw.nl/v5/");
  }

  @Test
  public void dataSetGeneratesAUri() {
    String dataSet = instance.dataSet("use&rId", "data&set");

    assertThat(dataSet, is("http://timbuctoo.huygens.knaw.nl/v5/datasets/use%26rId/data%26set"));
  }

}
