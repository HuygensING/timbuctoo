package nl.knaw.huygens.timbuctoo.v5.rml;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.ThrowingErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.datasource.jexl.JexlRowFactory;
import nl.knaw.huygens.timbuctoo.rml.datasource.joinhandlers.HashMapBasedJoinHandler;
import nl.knaw.huygens.timbuctoo.v5.bulkupload.RawUploadRdfSaver;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.NonPersistentBdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class RmlDataSourceStoreTest {

  @Test
  public void itWorks() throws Exception {
    NonPersistentBdbDatabaseCreator dbCreator = new NonPersistentBdbDatabaseCreator();
    PromotedDataSet dataSetMetadata = PromotedDataSet.promotedDataSet(
      "userid",
      "datasetid",
      "http://timbuctoo.huygens.knaw.nl/v5/userId/dataSetId",
      false
    );
    RmlDataSourceStoreTestDataProvider dataSet = new RmlDataSourceStoreTestDataProvider(dataSetMetadata);
    final RmlDataSourceStore rmlDataSourceStore = new RmlDataSourceStore(
      dataSetMetadata.getOwnerId(),
      dataSetMetadata.getDataSetId(),
      dbCreator,
      dataSet
    );
    RawUploadRdfSaver rawUploadRdfSaver = dataSet.getRawUploadRdfSaver();
    final String inputCol1 = rawUploadRdfSaver.addCollection("collection1");
    ImportPropertyDescriptions importPropertyDescriptions = new ImportPropertyDescriptions();
    importPropertyDescriptions.getOrCreate(1).setPropertyName("propName1");
    importPropertyDescriptions.getOrCreate(2).setPropertyName("propName2");
    rawUploadRdfSaver.addPropertyDescriptions(inputCol1, importPropertyDescriptions);
    rawUploadRdfSaver.addEntity(inputCol1, ImmutableMap.of("propName1", "value1", "propName2", "val2"));
    rawUploadRdfSaver.addEntity(inputCol1, ImmutableMap.of("propName1", "entVal1", "propName2", "entVal2"));
    final String inputCol2 = rawUploadRdfSaver.addCollection("collection2");
    ImportPropertyDescriptions importPropertyDescriptions1 = new ImportPropertyDescriptions();
    importPropertyDescriptions1.getOrCreate(1).setPropertyName("prop3");
    importPropertyDescriptions1.getOrCreate(2).setPropertyName("prop4");
    rawUploadRdfSaver.addPropertyDescriptions(inputCol2, importPropertyDescriptions1);
    rawUploadRdfSaver.addEntity(inputCol2, ImmutableMap.of("prop3", "value1", "prop4", "val2"));
    rawUploadRdfSaver.addEntity(inputCol2, ImmutableMap.of("prop3", "entVal1", "prop4", "entVal2"));



    RdfDataSource rdfDataSource = new RdfDataSource(rmlDataSourceStore,
      "http://timbuctoo.huygens.knaw.nl/v5/userId/dataSetId/rawData/fileName/collections/1",
      new JexlRowFactory(ImmutableMap.of(), new HashMapBasedJoinHandler())
    );
    RdfDataSource rdfDataSource2 = new RdfDataSource(rmlDataSourceStore,
      "http://timbuctoo.huygens.knaw.nl/v5/userId/dataSetId/rawData/fileName/collections/2",
      new JexlRowFactory(ImmutableMap.of(), new HashMapBasedJoinHandler())
    );

    final List<String> collection1;
    final List<String> collection2;
    try (Stream<Row> stream = rdfDataSource.getRows(new ThrowingErrorHandler())) {
      collection1 = stream
        .map(x -> x.getRawValue("propName1") + ":" + x.getRawValue("propName2"))
        .collect(toList());
    }
    try (Stream<Row> stream = rdfDataSource2.getRows(new ThrowingErrorHandler())) {
      collection2 = stream
        .map(x -> x.getRawValue("prop3") + ":" + x.getRawValue("prop4"))
        .collect(toList());
    }

    assertThat(collection1, contains("value1:val2", "entVal1:entVal2"));
    assertThat(collection2, contains("value1:val2", "entVal1:entVal2"));
    dbCreator.close();
  }

  private static class RmlDataSourceStoreTestDataProvider implements DataProvider {

    private final PromotedDataSet dataSetMetadata;
    private RawUploadRdfSaver rawUploadRdfSaver;

    public RmlDataSourceStoreTestDataProvider(PromotedDataSet dataSetMetadata) {
      this.dataSetMetadata = dataSetMetadata;
    }

    @Override
    public void subscribeToRdf(RdfProcessor processor) {
      RdfSerializer rdfSerializer = new RmlDataSourceRdfSerializer(processor);

      try {
        rawUploadRdfSaver = new RawUploadRdfSaver(
          dataSetMetadata,
          "fileName",
          APPLICATION_OCTET_STREAM_TYPE,
          rdfSerializer
        );
        processor.start(0);
      } catch (RdfProcessingFailedException | LogStorageFailedException e) {
        throw new RuntimeException(e.getCause());
      }
    }

    @Override
    public void subscribeToEntities(EntityProcessor processor) {

    }

    public RawUploadRdfSaver getRawUploadRdfSaver() {
      return rawUploadRdfSaver;
    }
  }

  private static class RmlDataSourceRdfSerializer implements RdfSerializer {
    private final RdfProcessor processor;

    public RmlDataSourceRdfSerializer(RdfProcessor processor) {
      this.processor = processor;
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
      try {
        processor.addRelation(subject, predicate, object, graph);
      } catch (RdfProcessingFailedException e) {
        throw new LogStorageFailedException(e.getCause());
      }
    }

    @Override
    public void onValue(String subject, String predicate, String value, String valueType, String graph)
      throws LogStorageFailedException {
      try {
        processor.addValue(subject, predicate, value, valueType, graph);
      } catch (RdfProcessingFailedException e) {
        throw new LogStorageFailedException(e.getCause());
      }
    }

    @Override
    public void onLanguageTaggedString(String subject, String predicate, String value, String language,
                                       String graph)
      throws LogStorageFailedException {
      try {
        processor.addValue(subject, predicate, value, language, graph);
      } catch (RdfProcessingFailedException e) {
        throw new LogStorageFailedException(e.getCause());
      }
    }

    @Override
    public void close() throws LogStorageFailedException {
      try {
        processor.commit();
      } catch (RdfProcessingFailedException e) {
        throw new LogStorageFailedException(e.getCause());
      }
    }
  }
}
