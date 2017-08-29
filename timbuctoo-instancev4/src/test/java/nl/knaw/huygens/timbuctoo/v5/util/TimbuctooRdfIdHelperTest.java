package nl.knaw.huygens.timbuctoo.v5.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TimbuctooRdfIdHelperTest {

  @Test
  public void rawEntityGeneratesAUri() {
    String entity = TimbuctooRdfIdHelper.rawEntity("ownerId", "data&set", "file$name", 1);

    assertThat(entity, is("http://timbuctoo.huygens.knaw.nl/v5/rawData/ownerId/data%26set/file%24name/1"));
  }

  @Test
  public void rawCollectionGeneratesAUri() {
    String collection = TimbuctooRdfIdHelper.rawCollection("ownerId", "data&set", "file$name", 1);

    assertThat(collection, is("http://timbuctoo.huygens.knaw.nl/v5/collections/ownerId/data%26set/file%24name/1"));
  }


  @Test
  public void propertyDescriptionGeneratesAUri() {
    String propertyDescription = TimbuctooRdfIdHelper.propertyDescription("ownerId", "data&set", "file$name",
      "prop+name");

    assertThat(
      propertyDescription,
      is("http://timbuctoo.huygens.knaw.nl/v5/props/ownerId/data%26set/file%24name/prop%2Bname")
    );
  }

  @Test
  public void dataSetGeneratesAUri() {
    String dataSet = TimbuctooRdfIdHelper.dataSet("use&rId", "data&set");

    assertThat(dataSet, is("http://timbuctoo.huygens.knaw.nl/v5/datasets/use%26rId/data%26set"));
  }

}
