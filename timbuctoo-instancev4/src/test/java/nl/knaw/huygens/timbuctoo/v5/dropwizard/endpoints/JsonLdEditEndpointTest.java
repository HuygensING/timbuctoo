package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.DummyDataProvider;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.NonPersistentBdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.Entity;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.ImmutableEntity;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
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

    CursorQuad quad =
      CursorQuad
        .create("http://example/olddatasetuserid", RdfConstants.TIM_LATEST_REVISION_OF, Direction.OUT, "oldvalue1",
          STRING, null, "");


    MyTestRdfPatchSerializer myTestRdfPatchSerializer = new MyTestRdfPatchSerializer();

    JsonLdEditEndpoint jsonLdEditEndpoint =
      new JsonLdEditEndpoint(null, new ObjectMapper(), new TimbuctooRdfIdHelper("http://example.org"));

    assertThat(jsonLdEditEndpoint.lastRevisionCheck(testEntities, quadStore), is(true));
  }

  private class MyTestRdfPatchSerializer implements RdfPatchSerializer {
    String results = "";

    @Override
    public void delRelation(String subject, String predicate, String object, String graph)
      throws LogStorageFailedException {
      results += "- " + subject + " " + predicate + " " + object + "\n";
    }

    @Override
    public void delValue(String subject, String predicate, String value, String valueType, String graph)
      throws LogStorageFailedException {
      results += "- " + subject + " " + predicate + " " + value + "\n";
    }

    @Override
    public void delLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
      throws LogStorageFailedException {

    }

    @Override
    public MediaType getMediaType() {
      return null;
    }

    @Override
    public Charset getCharset() {
      return null;
    }

    @Override
    public void onPrefix(String prefix, String iri) throws LogStorageFailedException {

    }

    @Override
    public void onRelation(String subject, String predicate, String object, String graph)
      throws LogStorageFailedException {
      results += "+ " + subject + " " + predicate + " " + object + "\n";
    }

    @Override
    public void onValue(String subject, String predicate, String value, String valueType, String graph)
      throws LogStorageFailedException {
      results += "+ " + subject + " " + predicate + " " + value + "\n";
    }

    @Override
    public void onLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
      throws LogStorageFailedException {

    }

    @Override
    public void close() throws LogStorageFailedException {

    }
  }
}
