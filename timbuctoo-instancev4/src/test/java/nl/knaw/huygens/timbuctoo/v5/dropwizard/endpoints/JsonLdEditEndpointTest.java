package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.security.JsonBasedAuthorizer;
import nl.knaw.huygens.timbuctoo.security.dataaccess.localfile.LocalFileVreAuthorizationAccess;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbDataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.DummyDataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImmutableDataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.DummyDataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.NonPersistentBdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.Entity;
import nl.knaw.huygens.timbuctoo.v5.jsonldimport.ImmutableEntity;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.Executors;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

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

    File tempFile = Files.createTempDir();

    DataSetRepository dataSetFactory = new DataSetRepository(
      Executors.newSingleThreadExecutor(),
      new JsonBasedAuthorizer(new LocalFileVreAuthorizationAccess(tempFile.toPath())),
      ImmutableDataSetConfiguration.builder()
                                   .dataSetMetadataLocation(tempFile.getAbsolutePath())
                                   .rdfIo(mock(RdfIoFactory.class, RETURNS_DEEP_STUBS))
                                   .fileStorage(mock(FileStorageFactory.class, RETURNS_DEEP_STUBS))
                                   .resourceSync(mock(ResourceSync.class))
                                   .build(),
      new BdbDataStoreFactory(new NonPersistentBdbDatabaseCreator()));

    NonPersistentBdbDatabaseCreator databaseCreator = new NonPersistentBdbDatabaseCreator();
    DummyDataProvider dataProvider = new DummyDataProvider();

    final QuadStore quadStore = new BdbTripleStore(
      dataProvider,
      databaseCreator,
      "userId",
      "dataSetId"
    );

    dataProvider.start();
    dataProvider.onQuad("http://example/olddatasetuserid", RdfConstants.TIM_LATEST_REVISION_OF,
      "oldvalue1", null, null, "http://somegraph");
    dataProvider.finish();
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
