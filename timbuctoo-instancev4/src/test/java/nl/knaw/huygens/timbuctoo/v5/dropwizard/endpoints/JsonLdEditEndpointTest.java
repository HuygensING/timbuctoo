package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.DummyDataProvider;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.NonPersistentBdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.Entity;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.ImmutableEntity;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JsonLdEditEndpointTest {

  private BdbTripleStore quadStore;
  private DummyDataProvider dataProvider;

  @Before
  public void setup() throws DataStoreCreationException {
    dataProvider = new DummyDataProvider();
    NonPersistentBdbDatabaseCreator databaseCreator = new NonPersistentBdbDatabaseCreator();

    quadStore = new BdbTripleStore(
      dataProvider,
      databaseCreator,
      "userId",
      "dataSetId"
    );
  }

  @After
  public void teardown() {
    quadStore.close();
  }

  @Test
  public void testRevisionOfCheck() throws Exception {

    HashMap<String, URI> revisionOf = new HashMap<>();

    revisionOf.put("@id", URI.create("http://example/olddatasetuserid"));

    Entity testEntity = ImmutableEntity.builder()
                                       .entityType("test")
                                       .wasRevisionOf(revisionOf)
                                       .specializationOf(URI.create("http://example/datasetuserid"))
                                       .putReplacements("pred", new String[]{"value1", "value2"}).build();

    Entity testEntity2 = ImmutableEntity.builder()
                                        .entityType("test")
                                        .wasRevisionOf(revisionOf)
                                        .specializationOf(URI.create("http://example/datasetuserid"))
                                        .putReplacements("pred2", new String[]{"value3", "value4"}).build();

    Entity[] testEntities = new Entity[2];

    testEntities[0] = testEntity;
    testEntities[1] = testEntity2;

    dataProvider.start();
    dataProvider.onQuad("http://example/olddatasetuserid", RdfConstants.TIM_LATEST_REVISION_OF,
      "oldvalue1", null, null, "http://somegraph");
    dataProvider.onQuad("http://example/olddatasetuserid", RdfConstants.TIM_SPECIALIZATION_OF,
      "http://example/datasetuserid", null, null, "http://somegraph");
    dataProvider.finish();

    JsonLdEditEndpoint jsonLdEditEndpoint =
      new JsonLdEditEndpoint(null, null, null, new ObjectMapper(),
        new TimbuctooRdfIdHelper("http://example/datasetuserid"));

    assertThat(jsonLdEditEndpoint.lastRevisionCheck(testEntities, quadStore), is(true));
  }

}
