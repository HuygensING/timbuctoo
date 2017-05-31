package nl.knaw.huygens.timbuctoo.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;

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
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class RdfSaverTest {

  private static final String COLLECTION = "coll";
  private static final String DATA_SET_ID = "dataset";
  private RdfSaver instance;
  private RdfSerializer rdfSerializer;

  @Before
  public void setUp() throws Exception {
    rdfSerializer = mock(RdfSerializer.class);
    instance = new RdfSaver(DATA_SET_ID, "fileName", rdfSerializer);
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

    verify(rdfSerializer).onRelation(entity, RDF_TYPE, COLLECTION, DATA_SET_ID);
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
      eq(DATA_SET_ID)
    );
    verify(rdfSerializer).onValue(
      eq(entity),
      argThat(containsString("prop2")),
      eq("2"),
      eq(STRING),
      eq(DATA_SET_ID)
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

    verify(rdfSerializer).onValue(collection, RDFS_LABEL, COLLECTION, STRING, DATA_SET_ID);
  }

  @Test
  public void addCollectionAddsAnOrderPropertyToTheCollection() throws Exception {
    String collection = instance.addCollection(COLLECTION);

    // It is the first collection, so the value of the order will be "1".
    verify(rdfSerializer).onValue(collection, TIMBUCTOO_ORDER, "1", INTEGER, DATA_SET_ID);
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
      eq(DATA_SET_ID)
    );
    verify(rdfSerializer).onValue(
      argThat(containsString("propName")),
      eq(TIM_PROP_ID),
      eq("1"),
      eq(INTEGER),
      eq(DATA_SET_ID)
    );
    verify(rdfSerializer).onValue(
      argThat(containsString("propName")),
      eq(TIMBUCTOO_ORDER),
      eq("0"), // Order start with 0.
      eq(INTEGER),
      eq(DATA_SET_ID)
    );
    verify(rdfSerializer).onValue(
      argThat(containsString("propName")),
      eq(RDFS_LABEL),
      eq("propName"),
      eq(STRING),
      eq(DATA_SET_ID)
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
      eq(DATA_SET_ID)
    );
    verify(rdfSerializer).onRelation(
      argThat(containsString("propName2")),
      eq(OF_COLLECTION),
      eq(COLLECTION),
      eq(DATA_SET_ID)
    );
  }

  // TODO tests aanpassen dat er ook een regelnummer wordt meegegeven
}

