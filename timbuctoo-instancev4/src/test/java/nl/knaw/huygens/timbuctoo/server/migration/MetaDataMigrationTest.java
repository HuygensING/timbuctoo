package nl.knaw.huygens.timbuctoo.server.migration;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.server.migration.MetaDataMigration;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.BasicDataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonFileBackedData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static com.google.common.io.Files.createTempDir;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MetaDataMigrationTest {

  private File testDataSetsFolder;

  @Before
  public void setUp() throws Exception {
    testDataSetsFolder = createTempDir();
  }

  @After
  public void tearDown() {
    testDataSetsFolder.delete();
  }

  @Test
  public void metaDataMigrationCreatesMetaDataFiles() throws Exception {

    File testUserFolder = new File(testDataSetsFolder.toString() + "/u33707283d426f900d4d33707283d426f900d4d0d");
    testUserFolder.mkdir();
    File testDataSetDir1 = new File(testDataSetsFolder.toString() +
      "/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo2");
    testDataSetDir1.mkdir();
    File testDataSetDir2 = new File(testDataSetsFolder.toString() +
      "/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo3");
    testDataSetDir2.mkdir();

    File testUser2Folder = new File(testDataSetsFolder.toString() +
      "/u33707283d426f900d4d33707283d426f900d4e0e");
    testUser2Folder.mkdir();
    File testUser2DataSetDir1 = new File(testDataSetsFolder.toString() +
      "/u33707283d426f900d4d33707283d426f900d4e0e/hpp2demo2");
    testUser2DataSetDir1.mkdir();

    File file = new File(testDataSetsFolder, "dataSets.json");
    file.createNewFile();

    String testAuthInfo = "{\n" +
      "  \"u33707283d426f900d4d33707283d426f900d4d0d\" : [ {\n" +
      "    \"dataSetId\" : \"hpp6demo2\",\n" +
      "    \"ownerId\" : \"u33707283d426f900d4d33707283d426f900d4d0d\",\n" +
      "    \"baseUri\" : \"http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo2/\",\n" +
      "    \"uriPrefix\" : \"http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo2/\",\n" +
      "    \"combinedId\" : \"u33707283d426f900d4d33707283d426f900d4d0d__hpp6demo2\",\n" +
      "    \"isPromoted\" : false\n" +
      "  }, {\n" +
      "    \"dataSetId\" : \"hpp6demo3\",\n" +
      "    \"ownerId\" : \"u33707283d426f900d4d33707283d426f900d4d0d\",\n" +
      "    \"baseUri\" : \"http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo3/\",\n" +
      "    \"uriPrefix\" : \"http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo3/\",\n" +
      "    \"combinedId\" : \"u33707283d426f900d4d33707283d426f900d4d0d__hpp6demo3\",\n" +
      "    \"isPromoted\" : true\n" +
      "  }],\n" +
      "  \"u33707283d426f900d4d33707283d426f900d4e0e\" : [ {\n" +
      "    \"dataSetId\" : \"hpp2demo2\",\n" +
      "    \"ownerId\" : \"u33707283d426f900d4d33707283d426f900d4e0e\",\n" +
      "    \"baseUri\" : \"http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4e0e/hpp2demo2/\",\n" +
      "    \"uriPrefix\" : \"http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4e0e/hpp2demo2/\",\n" +
      "    \"combinedId\" : \"u33707283d426f900d4d33707283d426f900d4e0e__hpp2demo2\",\n" +
      "    \"isPromoted\" : false\n" +
      "  }]\n" +
      "}";

    Files.write(file.toPath(), testAuthInfo.getBytes());

    DataSetConfiguration configuration = mock(DataSetConfiguration.class);
    given(configuration.getDataSetMetadataLocation()).willReturn(testDataSetsFolder.toString());
    MetaDataMigration metaDataMigration = new MetaDataMigration(configuration);

    metaDataMigration.migrate();

    File generatedMetaDataFile1 = new File(testDataSetsFolder.toString() +
      "/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo2/metaData.json");
    assertThat(generatedMetaDataFile1.exists(), is(true));
    JsonFileBackedData<BasicDataSetMetaData> metaDataFromFile1 = JsonFileBackedData.getOrCreate(
      generatedMetaDataFile1,
      null,
      new TypeReference<BasicDataSetMetaData>() {
      });
    assertThat(metaDataFromFile1.getData(),
      allOf(hasProperty("dataSetId", is("hpp6demo2")),
        hasProperty("ownerId", is("u33707283d426f900d4d33707283d426f900d4d0d")),
        hasProperty("baseUri",
          is("http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo2/")),
        hasProperty("uriPrefix",
          is("http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo2/")),
        hasProperty("combinedId", is("u33707283d426f900d4d33707283d426f900d4d0d__hpp6demo2")),
        hasProperty("promoted", is(false)),
        hasProperty("published", is(false))
      ));


    File generatedMetaDataFile2 = new File(testDataSetsFolder.toString() +
      "/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo3/metaData.json");
    assertThat(generatedMetaDataFile2.exists(), is(true));
    JsonFileBackedData<BasicDataSetMetaData> metaDataFromFile2 = JsonFileBackedData.getOrCreate(
      generatedMetaDataFile2,
      null,
      new TypeReference<BasicDataSetMetaData>() {
      });
    assertThat(metaDataFromFile2.getData(),
      allOf(hasProperty("dataSetId", is("hpp6demo3")),
        hasProperty("ownerId", is("u33707283d426f900d4d33707283d426f900d4d0d")),
        hasProperty("baseUri",
          is("http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo3/")),
        hasProperty("uriPrefix",
          is("http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4d0d/hpp6demo3/")),
        hasProperty("combinedId", is("u33707283d426f900d4d33707283d426f900d4d0d__hpp6demo3")),
        hasProperty("promoted", is(true)),
        hasProperty("published", is(false))
      ));


    File generatedMetaDataFile3 = new File(testDataSetsFolder.toString() +
      "/u33707283d426f900d4d33707283d426f900d4e0e/hpp2demo2/metaData.json");
    assertThat(generatedMetaDataFile3.exists(), is(true));
    JsonFileBackedData<BasicDataSetMetaData> metaDataFromFile3 = JsonFileBackedData.getOrCreate(
      generatedMetaDataFile3,
      null,
      new TypeReference<BasicDataSetMetaData>() {
      });
    assertThat(metaDataFromFile3.getData(),
      allOf(hasProperty("dataSetId", is("hpp2demo2")),
        hasProperty("ownerId", is("u33707283d426f900d4d33707283d426f900d4e0e")),
        hasProperty("baseUri",
          is("http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4e0e/hpp2demo2/")),
        hasProperty("uriPrefix",
          is("http://example.org/datasets/u33707283d426f900d4d33707283d426f900d4e0e/hpp2demo2/")),
        hasProperty("combinedId", is("u33707283d426f900d4d33707283d426f900d4e0e__hpp2demo2")),
        hasProperty("promoted", is(false)),
        hasProperty("published", is(false))
      ));

  }
}
