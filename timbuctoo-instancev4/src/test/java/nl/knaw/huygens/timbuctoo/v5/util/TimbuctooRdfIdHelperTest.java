package nl.knaw.huygens.timbuctoo.v5.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TimbuctooRdfIdHelperTest {

  @Test
  public void rawEntityGeneratesAUri() {
    String entity = TimbuctooRdfIdHelper.rawEntity("data&set", "file$name", 1);

    assertThat(entity, is("http://timbuctoo.huygens.knaw.nl/rawData/data%26set/file%24name/1"));
  }

  @Test
  public void rawCollectionGeneratesAUri() {
    String collection = TimbuctooRdfIdHelper.rawCollection("data&set", "file$name", 1);

    assertThat(collection, is("http://timbuctoo/collections/data%26set/file%24name/1"));
  }


  @Test
  public void propertyDescriptionGeneratesAUri() {
    String propertyDescription = TimbuctooRdfIdHelper.propertyDescription("data&set", "file$name", "prop+name");

    assertThat(propertyDescription, is("http://timbuctoo/props/data%26set/file%24name/prop%2Bname"));
  }

  @Test
  public void dataSetGeneratesAUri() {
    String dataSet = TimbuctooRdfIdHelper.dataSet("data&set");

    assertThat(dataSet, is("http://timbuctoo/datasets/data%26set"));
  }

}
