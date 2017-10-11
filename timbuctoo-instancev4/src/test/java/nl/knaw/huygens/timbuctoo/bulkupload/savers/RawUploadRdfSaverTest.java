package nl.knaw.huygens.timbuctoo.bulkupload.savers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.v5.bulkupload.RawUploadRdfSaver;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.INTEGER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.OF_COLLECTION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.PROV_DERIVED_FROM;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDFS_LABEL;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIMBUCTOO_ORDER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_COLLECTION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_HAS_ROW;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_MIMETYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_DESC;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_ID;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_TABULAR_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class RawUploadRdfSaverTest {

  private static final String COLLECTION = "coll";
  private RawUploadRdfSaver instance;
  private RdfSerializer rdfSerializer;
  private PromotedDataSet dataSetMetadata;

  @Before
  public void setUp() throws Exception {
    rdfSerializer = mock(RdfSerializer.class);
    dataSetMetadata = PromotedDataSet.promotedDataSet(
      "userid",
      "dataset",
      "http://timbuctoo.huygens.knaw.nl/v5/datasets/userid/dataset",
      false
    );
    instance = instanceWithRdfSerializer(rdfSerializer, dataSetMetadata);
  }

  private RawUploadRdfSaver instanceWithRdfSerializer(RdfSerializer rdfSerializer, PromotedDataSet dataSetMetadata)
    throws LogStorageFailedException {
    return new RawUploadRdfSaver(dataSetMetadata, "fileName", APPLICATION_OCTET_STREAM_TYPE, rdfSerializer);
  }

  @Test
  public void addEntityReturnsARepresentationOfTheEntityThatIsNotNull() {
    String entity = instance.addEntity(COLLECTION, Maps.newHashMap());

    assertThat(entity, is(notNullValue()));
  }

  @Test
  public void addEntityReturnsADifferentRepresentationEachCall() {
    String entity1 = instance.addEntity(COLLECTION, Maps.newHashMap());
    String entity2 = instance.addEntity(COLLECTION, Maps.newHashMap());

    assertThat(entity1, is(not(entity2)));
  }

  @Test
  public void addEntityAddsTheEntityToTheCollection() throws Exception {
    String entity = instance.addEntity(COLLECTION, Maps.newHashMap());

    verify(rdfSerializer).onRelation(entity, TIM_HAS_ROW, COLLECTION, dataSetMetadata.getBaseUri());
  }

  @Test
  public void addEntityAddsAStringPropertyForEachPropertyOfTheEntity() throws Exception {
    Map<String, Object> properties = Maps.newHashMap();
    properties.put("prop1", "value1");
    properties.put("prop2", 2);

    String entity = instance.addEntity(COLLECTION, properties);

    verify(rdfSerializer).onValue(
      eq(entity),
      argThat(containsString("prop1")),
      eq("value1"),
      eq(STRING),
      eq(dataSetMetadata.getBaseUri())
    );
    verify(rdfSerializer).onValue(
      eq(entity),
      argThat(containsString("prop2")),
      eq("2"),
      eq(STRING),
      eq(dataSetMetadata.getBaseUri())
    );
  }

  @Test
  public void addCollectionReturnsARepresentationOfACollectionThatIsNotNull() {
    String collection = instance.addCollection(COLLECTION);

    assertThat(collection, is(notNullValue()));
  }

  @Test
  public void addCollectionReturnsAUniqueRepresentationEachTime() {
    String collection1 = instance.addCollection(COLLECTION);
    String collection2 = instance.addCollection(COLLECTION);

    assertThat(collection1, is(not(collection2)));
  }

  @Test
  public void addCollectionAddsALabelWithTheCollectionName() throws Exception {
    String collection = instance.addCollection(COLLECTION);

    verify(rdfSerializer).onValue(collection, RDFS_LABEL, COLLECTION, STRING, dataSetMetadata.getBaseUri());
  }

  @Test
  public void addCollectionAddsAnOrderPropertyToTheCollection() throws Exception {
    String collection = instance.addCollection(COLLECTION);

    // It is the first collection, so the value of the order will be "1".
    verify(rdfSerializer).onValue(collection, TIMBUCTOO_ORDER, "1", INTEGER, dataSetMetadata.getBaseUri());
  }

  @Test
  public void addPropertyDescriptionCreatesAPropertyDescription() throws Exception {
    ImportPropertyDescriptions importPropertyDescriptions = new ImportPropertyDescriptions();
    importPropertyDescriptions.getOrCreate(1).setPropertyName("propName");

    instance.addPropertyDescriptions(COLLECTION, importPropertyDescriptions);

    verify(rdfSerializer).onRelation(
      argThat(containsString("propName")),
      eq(RDF_TYPE),
      eq(TIM_PROP_DESC),
      eq(dataSetMetadata.getBaseUri())
    );
    verify(rdfSerializer).onValue(
      argThat(containsString("propName")),
      eq(TIM_PROP_ID),
      eq("1"),
      eq(INTEGER),
      eq(dataSetMetadata.getBaseUri())
    );
    verify(rdfSerializer).onValue(
      argThat(containsString("propName")),
      eq(TIMBUCTOO_ORDER),
      eq("0"), // Order start with 0.
      eq(INTEGER),
      eq(dataSetMetadata.getBaseUri())
    );
    verify(rdfSerializer).onValue(
      argThat(containsString("propName")),
      eq(RDFS_LABEL),
      eq("propName"),
      eq(STRING),
      eq(dataSetMetadata.getBaseUri())
    );
  }

  @Test
  public void addPropertyDescriptionAddsAllThePropertyDescriptionsToTheCollection() throws Exception {
    ImportPropertyDescriptions importPropertyDescriptions = new ImportPropertyDescriptions();
    importPropertyDescriptions.getOrCreate(1).setPropertyName("propName1");
    importPropertyDescriptions.getOrCreate(2).setPropertyName("propName2");

    instance.addPropertyDescriptions(COLLECTION, importPropertyDescriptions);
  
    verify(rdfSerializer).onRelation(
      argThat(containsString("propName1")),
      eq(OF_COLLECTION),
      eq(COLLECTION),
      eq(dataSetMetadata.getBaseUri())
    );
    verify(rdfSerializer).onRelation(
      argThat(containsString("propName2")),
      eq(OF_COLLECTION),
      eq(COLLECTION),
      eq(dataSetMetadata.getBaseUri())
    );
  }

  @Test
  public void usageTest() throws LogStorageFailedException {
    RdfToStringFaker rdfSerializer = new RdfToStringFaker();
    RawUploadRdfSaver instance = instanceWithRdfSerializer(rdfSerializer, dataSetMetadata);

    final String collection1 = instance.addCollection("collection1");
    ImportPropertyDescriptions importPropertyDescriptions = new ImportPropertyDescriptions();
    importPropertyDescriptions.getOrCreate(1).setPropertyName("propName1");
    importPropertyDescriptions.getOrCreate(2).setPropertyName("propName2");
    instance.addPropertyDescriptions(collection1, importPropertyDescriptions);
    instance.addEntity(collection1, ImmutableMap.of("propName1", "value1", "propName2", "val2"));
    instance.addEntity(collection1, ImmutableMap.of("propName1", "entVal1", "propName2", "entVal2"));
    final String collection2 = instance.addCollection("collection2");
    ImportPropertyDescriptions importPropertyDescriptions1 = new ImportPropertyDescriptions();
    importPropertyDescriptions1.getOrCreate(1).setPropertyName("prop3");
    importPropertyDescriptions1.getOrCreate(2).setPropertyName("prop4");
    instance.addPropertyDescriptions(collection2, importPropertyDescriptions1);
    instance.addEntity(collection2, ImmutableMap.of("prop3", "value1", "prop4", "val2"));
    instance.addEntity(collection2, ImmutableMap.of("prop3", "entVal1", "prop4", "entVal2"));

    String generatedRdf = rdfSerializer.toString();
    // Use assertEquals because the failing Hamcrest output is hard to compare
    String graphName = dataSetMetadata.getBaseUri();
    String fileUri = dataSetMetadata.getBaseUri() + "/rawData/fileName/";
    String collection = fileUri + "collections/";
    String prop = fileUri + "props/";
    String rawData = fileUri + "entities/";
    assertEquals(
      fileUri + " "         + RDF_TYPE           + " " + TIM_TABULAR_FILE + " "                  + graphName + "\n" +
        graphName + " "     + PROV_DERIVED_FROM  + " " + fileUri + " "                           + graphName + "\n" +
        fileUri + " "    + TIM_MIMETYPE + " " + "application/octet-stream" + "^^" + STRING + " " + graphName + "\n" +
        collection + "1 "   + RDF_TYPE           + " " + TIM_COLLECTION +   " "                  + graphName + "\n" +
        collection + "1 "   + RDFS_LABEL         + " collection1" +         "^^" + STRING + " "  + graphName + "\n" +
        collection + "1 "   + TIMBUCTOO_ORDER    + " 1" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "propName1 " + RDF_TYPE           + " " + TIM_PROP_DESC + " "                     + graphName + "\n" +
        prop + "propName1 " + TIM_PROP_ID        + " 1" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "propName1 " + TIMBUCTOO_ORDER    + " 0" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "propName1 " + RDFS_LABEL         + " propName1" +           "^^" + STRING + " "  + graphName + "\n" +
        prop + "propName1 " + TIM_PROP_NAME      + " propName1" +           "^^" + STRING + " "  + graphName + "\n" +
        prop + "propName1 " + OF_COLLECTION      + " " + collection + "1 "                       + graphName + "\n" +
        prop + "propName2 " + RDF_TYPE           + " " + TIM_PROP_DESC + " "                     + graphName + "\n" +
        prop + "propName2 " + TIM_PROP_ID        + " 2" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "propName2 " + TIMBUCTOO_ORDER    + " 1" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "propName2 " + RDFS_LABEL         + " propName2" +           "^^" + STRING + " "  + graphName + "\n" +
        prop + "propName2 " + TIM_PROP_NAME      + " propName2" +           "^^" + STRING + " "  + graphName + "\n" +
        prop + "propName2 " + OF_COLLECTION      + " " + collection + "1 "                       + graphName + "\n" +
        prop + "tim_id "    + RDF_TYPE           + " " + TIM_PROP_DESC + " "                     + graphName + "\n" +
        prop + "tim_id "    + TIM_PROP_ID        + " -1" +                  "^^" + INTEGER + " " + graphName + "\n" +
        prop + "tim_id "    + TIMBUCTOO_ORDER    + " -1" +                  "^^" + INTEGER + " " + graphName + "\n" +
        prop + "tim_id "    + RDFS_LABEL         + " tim_id" +              "^^" + STRING + " "  + graphName + "\n" +
        prop + "tim_id "    + TIM_PROP_NAME      + " tim_id" +              "^^" + STRING + " "  + graphName + "\n" +
        prop + "tim_id "    + OF_COLLECTION      + " " + collection + "1 "                       + graphName + "\n" +
        rawData + "1 "      + RDF_TYPE           + " " + collection + "1 "                       + graphName + "\n" +
        rawData + "1 "      + TIM_HAS_ROW        + " " + collection + "1 "                       + graphName + "\n" +
        rawData + "1 "      + prop + "propName1" + " value1" +              "^^" + STRING + " "  + graphName + "\n" +
        rawData + "1 "      + prop + "propName2" + " val2" +                "^^" + STRING + " "  + graphName + "\n" +
        rawData + "1 "      + prop + "tim_id"    + " {UUID}" +              "^^" + STRING + " "  + graphName + "\n" +
        rawData + "2 "      + RDF_TYPE           + " " + collection + "1 "                       + graphName + "\n" +
        rawData + "2 "      + TIM_HAS_ROW        + " " + collection + "1 "                       + graphName + "\n" +
        rawData + "2 "      + prop + "propName1" + " entVal1" +             "^^" + STRING + " "  + graphName + "\n" +
        rawData + "2 "      + prop + "propName2" + " entVal2" +             "^^" + STRING + " "  + graphName + "\n" +
        rawData + "2 "      + prop + "tim_id"    + " {UUID}" +              "^^" + STRING + " "  + graphName + "\n" +
        collection + "2 "   + RDF_TYPE           + " " + TIM_COLLECTION + " "                    + graphName + "\n" +
        collection + "2 "   + RDFS_LABEL         + " collection2" +         "^^" + STRING + " "  + graphName + "\n" +
        collection + "2 "   + TIMBUCTOO_ORDER    + " 2" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "prop3 "     + RDF_TYPE           + " " + TIM_PROP_DESC + " "                     + graphName + "\n" +
        prop + "prop3 "     + TIM_PROP_ID        + " 1" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "prop3 "     + TIMBUCTOO_ORDER    + " 0" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "prop3 "     + RDFS_LABEL         + " prop3" +               "^^" + STRING + " "  + graphName + "\n" +
        prop + "prop3 "     + TIM_PROP_NAME      + " prop3" +               "^^" + STRING + " "  + graphName + "\n" +
        prop + "prop3 "     + OF_COLLECTION      + " " + collection + "2 "                       + graphName + "\n" +
        prop + "prop4 "     + RDF_TYPE           + " " + TIM_PROP_DESC + " "                     + graphName + "\n" +
        prop + "prop4 "     + TIM_PROP_ID        + " 2" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "prop4 "     + TIMBUCTOO_ORDER    + " 1" +                   "^^" + INTEGER + " " + graphName + "\n" +
        prop + "prop4 "     + RDFS_LABEL         + " prop4" +               "^^" + STRING + " "  + graphName + "\n" +
        prop + "prop4 "     + TIM_PROP_NAME      + " prop4" +               "^^" + STRING + " "  + graphName + "\n" +
        prop + "prop4 "     + OF_COLLECTION      + " " + collection + "2 "                       + graphName + "\n" +
        prop + "tim_id "    + RDF_TYPE           + " " + TIM_PROP_DESC + " "                     + graphName + "\n" +
        prop + "tim_id "    + TIM_PROP_ID        + " -1" +                  "^^" + INTEGER + " " + graphName + "\n" +
        prop + "tim_id "    + TIMBUCTOO_ORDER    + " -1" +                  "^^" + INTEGER + " " + graphName + "\n" +
        prop + "tim_id "    + RDFS_LABEL         + " tim_id" +              "^^" + STRING + " "  + graphName + "\n" +
        prop + "tim_id "    + TIM_PROP_NAME      + " tim_id" +              "^^" + STRING + " "  + graphName + "\n" +
        prop + "tim_id "    + OF_COLLECTION      + " " + collection + "2 "                       + graphName + "\n" +
        rawData + "3 "      + RDF_TYPE           + " " + collection + "2 "                       + graphName + "\n" +
        rawData + "3 "      + TIM_HAS_ROW        + " " + collection + "2 "                       + graphName + "\n" +
        rawData + "3 "      + prop + "prop3"     + " value1" +              "^^" + STRING + " "  + graphName + "\n" +
        rawData + "3 "      + prop + "prop4"     + " val2" +                "^^" + STRING + " "  + graphName + "\n" +
        rawData + "3 "      + prop + "tim_id"    + " {UUID}" +              "^^" + STRING + " "  + graphName + "\n" +
        rawData + "4 "      + RDF_TYPE           + " " + collection + "2 "                       + graphName + "\n" +
        rawData + "4 "      + TIM_HAS_ROW        + " " + collection + "2 "                       + graphName + "\n" +
        rawData + "4 "      + prop + "prop3"     + " entVal1" +             "^^" + STRING + " "  + graphName + "\n" +
        rawData + "4 "      + prop + "prop4"     + " entVal2" +             "^^" + STRING + " "  + graphName + "\n" +
        rawData + "4 "      + prop + "tim_id"    + " {UUID}" +              "^^" + STRING + " "  + graphName + "\n",
        generatedRdf.replaceAll("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "{UUID}")
    );
  }

  private static class RdfToStringFaker implements RdfSerializer {

    private final StringBuilder stringBuilder;

    public RdfToStringFaker() {
      stringBuilder = new StringBuilder();
    }

    @Override
    public MediaType getMediaType() {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Charset getCharset() {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void onPrefix(String prefix, String iri) throws LogStorageFailedException {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void onRelation(String subject, String predicate, String object, String graph)
      throws LogStorageFailedException {
      stringBuilder.append(String.format("%s %s %s %s%s", subject, predicate, object, graph,
        System.lineSeparator()));
    }

    @Override
    public void onValue(String subject, String predicate, String value, String valueType, String graph)
      throws LogStorageFailedException {
      stringBuilder.append(String.format("%s %s %s^^%s %s%s", subject, predicate, value, valueType, graph,
        System.lineSeparator()));
    }

    @Override
    public void onLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
      throws LogStorageFailedException {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void close() throws LogStorageFailedException {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String toString() {
      return stringBuilder.toString();
    }
  }
}

