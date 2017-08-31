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
  public void rawEntityGeneratesAUri() {
    String entity = instance.rawEntity("ownerId", "data&set", "file$name", 1);

    assertThat(entity, is("http://timbuctoo.huygens.knaw.nl/v5/rawData/ownerId/data%26set/file%24name/1"));
  }

  @Test
  public void rawFileGeneratesUri() {
    String entity = instance.rawFile("ownerId", "data&set", "file$name");

    assertThat(entity, is("http://timbuctoo.huygens.knaw.nl/v5/rawData/ownerId/data%26set/file%24name/"));
  }

  @Test
  public void rawCollectionGeneratesAUri() {
    String collection = instance.rawCollection("ownerId", "data&set", "file$name", 1);

    assertThat(collection, is("http://timbuctoo.huygens.knaw.nl/v5/collections/ownerId/data%26set/file%24name/1"));
  }


  @Test
  public void propertyDescriptionGeneratesAUri() {
    String propertyDescription = instance.propertyDescription("ownerId", "data&set", "file$name",
      "prop+name");

    assertThat(
      propertyDescription,
      is("http://timbuctoo.huygens.knaw.nl/v5/props/ownerId/data%26set/file%24name/prop%2Bname")
    );
  }

  @Test
  public void dataSetGeneratesAUri() {
    String dataSet = instance.dataSet("use&rId", "data&set");

    assertThat(dataSet, is("http://timbuctoo.huygens.knaw.nl/v5/datasets/use%26rId/data%26set"));
  }

}
