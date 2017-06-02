package nl.knaw.huygens.timbuctoo.bulkupload.savers;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.INTEGER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.OF_COLLECTION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDFS_LABEL;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIMBUCTOO_ORDER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_DESC;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_ID;
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

public class RdfSaverTest {

  private static final String COLLECTION = "coll";
  private static final String DATA_SET_ID = "dataSet";
  private static final String DATA_SET_URI = "http://timbuctoo/datasets/dataSet";
  private RdfSaver instance;
  private RdfSerializer rdfSerializer;

  @Before
  public void setUp() throws Exception {
    rdfSerializer = mock(RdfSerializer.class);
    instance = instanceWithRdfSerializer(rdfSerializer);
  }

  private RdfSaver instanceWithRdfSerializer(RdfSerializer rdfSerializer) {
    return new RdfSaver(DATA_SET_ID, "fileName", rdfSerializer);
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

    verify(rdfSerializer).onRelation(entity, RDF_TYPE, COLLECTION, DATA_SET_URI);
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
      eq(DATA_SET_URI)
    );
    verify(rdfSerializer).onValue(
      eq(entity),
      argThat(containsString("prop2")),
      eq("2"),
      eq(STRING),
      eq(DATA_SET_URI)
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

    verify(rdfSerializer).onValue(collection, RDFS_LABEL, COLLECTION, STRING, DATA_SET_URI);
  }

  @Test
  public void addCollectionAddsAnOrderPropertyToTheCollection() throws Exception {
    String collection = instance.addCollection(COLLECTION);

    // It is the first collection, so the value of the order will be "1".
    verify(rdfSerializer).onValue(collection, TIMBUCTOO_ORDER, "1", INTEGER, DATA_SET_URI);
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
      eq(DATA_SET_URI)
    );
    verify(rdfSerializer).onValue(
      argThat(containsString("propName")),
      eq(TIM_PROP_ID),
      eq("1"),
      eq(INTEGER),
      eq(DATA_SET_URI)
    );
    verify(rdfSerializer).onValue(
      argThat(containsString("propName")),
      eq(TIMBUCTOO_ORDER),
      eq("0"), // Order start with 0.
      eq(INTEGER),
      eq(DATA_SET_URI)
    );
    verify(rdfSerializer).onValue(
      argThat(containsString("propName")),
      eq(RDFS_LABEL),
      eq("propName"),
      eq(STRING),
      eq(DATA_SET_URI)
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
      eq("http://timbuctoo.com/thing/ofCollection"),
      eq(COLLECTION),
      eq(DATA_SET_URI)
    );
    verify(rdfSerializer).onRelation(
      argThat(containsString("propName2")),
      eq(OF_COLLECTION),
      eq(COLLECTION),
      eq(DATA_SET_URI)
    );
  }

  @Test
  public void usageTest() {
    RdfToStringFaker rdfSerializer = new RdfToStringFaker();
    RdfSaver instance = instanceWithRdfSerializer(rdfSerializer);

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
    instance.addPropertyDescriptions(collection1, importPropertyDescriptions1);
    instance.addEntity(collection2, ImmutableMap.of("prop3", "value1", "prop4", "val2"));
    instance.addEntity(collection2, ImmutableMap.of("prop3", "entVal1", "prop4", "entVal2"));

    String generatedRdf = rdfSerializer.toString();
    // Use assertEquals because the failing Hamcrest output is hard to compare
    assertEquals(generatedRdf,
      "http://timbuctoo/collections/dataSet/fileName/1 http://rdfs/label collection1^^http://www" +
        ".w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/collections/dataSet/fileName/1 http://timbuctoo.com/things/order 1^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName1 http://www.w3.org/1999/02/22-rdf-syntax-ns#type " +
        "http://timbuctoo.com/things/propertyDescription/ http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName1 http://timbuctoo.com/things/propertyId 1^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName1 http://timbuctoo.com/things/order 0^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName1 http://rdfs/label propName1^^http://www" +
        ".w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName1 http://timbuctoo.com/thing/ofCollection " +
        "http://timbuctoo/collections/dataSet/fileName/1 http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName2 http://www.w3.org/1999/02/22-rdf-syntax-ns#type " +
        "http://timbuctoo.com/things/propertyDescription/ http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName2 http://timbuctoo.com/things/propertyId 2^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName2 http://timbuctoo.com/things/order 1^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName2 http://rdfs/label propName2^^http://www" +
        ".w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/propName2 http://timbuctoo.com/thing/ofCollection " +
        "http://timbuctoo/collections/dataSet/fileName/1 http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/1 http://www.w3.org/1999/02/22-rdf-syntax-ns#type " +
        "http://timbuctoo/collections/dataSet/fileName/1 http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/1 " +
        "http://timbuctoo/props/dataSet/fileName/propName1 value1^^http://www.w3.org/2001/XMLSchema#string " +
        "http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/1 " +
        "http://timbuctoo/props/dataSet/fileName/propName2 val2^^http://www.w3.org/2001/XMLSchema#string " +
        "http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/2 http://www.w3.org/1999/02/22-rdf-syntax-ns#type " +
        "http://timbuctoo/collections/dataSet/fileName/1 http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/2 " +
        "http://timbuctoo/props/dataSet/fileName/propName1 entVal1^^http://www.w3.org/2001/XMLSchema#string " +
        "http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/2 " +
        "http://timbuctoo/props/dataSet/fileName/propName2 entVal2^^http://www.w3.org/2001/XMLSchema#string " +
        "http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/collections/dataSet/fileName/2 http://rdfs/label collection2^^http://www" +
        ".w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/collections/dataSet/fileName/2 http://timbuctoo.com/things/order 2^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop3 http://www.w3.org/1999/02/22-rdf-syntax-ns#type " +
        "http://timbuctoo.com/things/propertyDescription/ http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop3 http://timbuctoo.com/things/propertyId 1^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop3 http://timbuctoo.com/things/order 0^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop3 http://rdfs/label prop3^^http://www" +
        ".w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop3 http://timbuctoo.com/thing/ofCollection " +
        "http://timbuctoo/collections/dataSet/fileName/1 http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop4 http://www.w3.org/1999/02/22-rdf-syntax-ns#type " +
        "http://timbuctoo.com/things/propertyDescription/ http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop4 http://timbuctoo.com/things/propertyId 2^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop4 http://timbuctoo.com/things/order 1^^http://www" +
        ".w3.org/2001/XMLSchema#integer http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop4 http://rdfs/label prop4^^http://www" +
        ".w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo/props/dataSet/fileName/prop4 http://timbuctoo.com/thing/ofCollection " +
        "http://timbuctoo/collections/dataSet/fileName/1 http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/3 http://www.w3.org/1999/02/22-rdf-syntax-ns#type " +
        "http://timbuctoo/collections/dataSet/fileName/2 http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/3 http://timbuctoo/props/dataSet/fileName/prop3 " +
        "value1^^http://www.w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/3 http://timbuctoo/props/dataSet/fileName/prop4 " +
        "val2^^http://www.w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/4 http://www.w3.org/1999/02/22-rdf-syntax-ns#type " +
        "http://timbuctoo/collections/dataSet/fileName/2 http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/4 http://timbuctoo/props/dataSet/fileName/prop3 " +
        "entVal1^^http://www.w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n" +
        "http://timbuctoo.huygens.knaw.nl/rawData/dataSet/fileName/4 http://timbuctoo/props/dataSet/fileName/prop4 " +
        "entVal2^^http://www.w3.org/2001/XMLSchema#string http://timbuctoo/datasets/dataSet\n"
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

