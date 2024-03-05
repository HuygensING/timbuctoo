package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import com.google.common.collect.Lists;
import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringIntegerIsCleanHandler;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.dropwizard.BdbNonPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.filestorage.ChangeLogStorage;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.ChangeType.ASSERTED;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.ChangeType.RETRACTED;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction.IN;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.PredicateMatcher.predicate;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.RDFS_RESOURCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class SchemaGenerationTest {
  private static final String USER = "user";
  private static final String DATA_SET = "dataSet";
  private static final StringStringIsCleanHandler STRING_IS_CLEAN_HANDLER = new StringStringIsCleanHandler();
  private static final StringIntegerIsCleanHandler STRING_INT_IS_CLEAN_HANDLER = new StringIntegerIsCleanHandler();
  private static final TupleBinding<String> STRING_BINDING = TupleBinding.getPrimitiveBinding(String.class);
  private static final TupleBinding<Integer> INT_BINDING = TupleBinding.getPrimitiveBinding(Integer.class);

  private static final String SUBJECT_A = "http://example.org/foo";
  private static final String SUBJECT_B = "http://example.org/bar";
  private static final String SUBJECT_C = "http://example.org/baz";

  private static final String TYPE_1 = "http://example.org/type";
  private static final String TYPE_2 = "http://example.org/footype";
  private static final String TYPE_3 = "http://example.org/barType";
  private static final String TYPE_4 = "http://example.org/baztype";

  private static final String PROP_I = "http://example.org/pred1";
  private static final String PROP_II = "http://example.org/pred2";
  private static final String PROP_III = "http://example.org/links";

  private static final String GRAPH_1 = "http://example.org/graph1";
  private static final String GRAPH_2 = "http://example.org/graph2";
  private static final String GRAPH_3 = "http://example.org/graph3";

  private static final String VALUE_TYPE = "http://example.org/valuetype";

  @Test
  public void everyTypeOfTheSubjectsIsAddedToTheSchema() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_1, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_B, RDF_TYPE, OUT, ASSERTED, TYPE_3, null, null, null, "")
    );
    assertThat(schema, allOf(
      hasEntry(is(TYPE_1), hasProperty("name", is(TYPE_1))),
      hasEntry(is(TYPE_1), hasProperty("name", is(TYPE_1))),
      hasEntry(is(TYPE_3), hasProperty("name", is(TYPE_3)))
    ));
  }

  /* Every predicate of SUBJECT_A (PROP_I, PROP_II) will be added to TYPE_1 and TYPE_2
   */
  @Test
  public void predicateOfASubjectIsAddedToEachType() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_1, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_II, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    );

    assertThat(schema, allOf(
      hasEntry(is(TYPE_1), hasProperty("predicates",allOf(
        hasItem(predicate().withName(PROP_I)),
        hasItem(predicate().withName(PROP_II))
      ))),
      hasEntry(is(TYPE_2), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I)),
        hasItem(predicate().withName(PROP_II))
      )))
    ));
  }

  /* PROP_I of SUBJECT_A should be added to TYPE_2 as an outgoing predicate with a reference type TYPE_3
   * PROP_I of SUBJECT_A should be added to TYPE_3 as an incoming predicate with a reference type TYPE_2
   */
  @Test
  public void eachPredicateThatLinksToAnotherSubjectWillBeAddedToTheOtherSubjectAsAnIncoming() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_B, RDF_TYPE, OUT, ASSERTED, TYPE_3, null, null, null, "")

    );

    assertThat(schema, allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates",allOf(
        hasItem(predicate().withName(PROP_I).withDirection(OUT).withReferenceType(TYPE_3))
      ))),
      hasEntry(is(TYPE_3), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I).withDirection(Direction.IN).withReferenceType(TYPE_2))
      )))
    ));
  }

  /* PROP_I of SUBJECT_A should be added to TYPE_2 as an outgoing predicate with a reference type TYPE_3
   * PROP_I of SUBJECT_A should be added to TYPE_3 as an incoming predicate with a reference type TYPE_2
   */
  @Test
  public void eachPredicateThatLinksToAnotherSubjectWillBeAddedToTheOtherSubjectAsAnIncomingEvenInMultipleSessions()
    throws Exception {
    BdbNonPersistentEnvironmentCreator dataStoreFactory = new BdbNonPersistentEnvironmentCreator();
    final BdbSchemaStore schema = new BdbSchemaStore(
      new BdbBackedData(dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "schema",
        false,
        STRING_BINDING,
        STRING_BINDING,
        STRING_IS_CLEAN_HANDLER
      )),
      mock(ImportStatus.class)
    );

    final StoreUpdater storeUpdater = createInstance(dataStoreFactory, schema);

    storeUpdater.start(0, false);
    storeUpdater.onQuad(true, SUBJECT_A, RDF_TYPE, TYPE_2, null, null, null);
    storeUpdater.onQuad(true, SUBJECT_A, PROP_I, SUBJECT_B, null, null, null);
    storeUpdater.commit();
    storeUpdater.start(1, false);
    storeUpdater.onQuad(true, SUBJECT_B, RDF_TYPE, TYPE_3, null, null, null);
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates",allOf(
        hasItem(predicate().withName(PROP_I).withDirection(OUT).withReferenceType(TYPE_3))
      ))),
      hasEntry(is(TYPE_3), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I).withDirection(Direction.IN).withReferenceType(TYPE_2))
      )))
    ));
  }

  /* PROP_I of SUBJECT_A should be added to TYPE_2 as an outgoing predicate with a reference type UKNOWN
   * PROP_I of SUBJECT_A should be added to UKNOWN as an incoming predicate with a reference type TYPE_2
   */
  @Test
  public void ifTheReferencedSubjectHasNoTypeThePredicateWillBeAddedToRdfsResource() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    );

    assertThat(schema, allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates",allOf(
        hasItem(predicate().withName(PROP_I).withDirection(OUT).withReferenceType(RdfConstants.RDFS_RESOURCE))
      ))),
      hasEntry(is(RdfConstants.RDFS_RESOURCE), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I).withDirection(Direction.IN).withReferenceType(TYPE_2))
      )))
    ));
  }

  /* Predicate PROP_I of SUBJECT_A has value type ex:valuetype and SUBJECT_A has rdf:type TYPE_2.
   * The schema should have an entry for TYPE_2 and
   * that entry should have a predicate for PROP_I with valuetype ex:valuetype.
   */
  @Test
  public void theValueTypeIsAddedToThePredicate() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, "value", VALUE_TYPE, null, null, "")
    );
    assertThat(schema, hasEntry(is(TYPE_2), hasProperty("predicates",
      hasItem(predicate().withName(PROP_I).withValueType(VALUE_TYPE))
    )));
  }

  /* SUBJECT_A has multiple predicate PROP_I pointing to multiple values
   * This should make predicate PROP_I a list predicate
   */
  @Test
  public void thePredicateWillBecomeAListWhenASubjectHasMultipleInstances() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, "value", VALUE_TYPE, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, "value2", VALUE_TYPE, null, null, "")
    );
    assertThat(schema, hasEntry(is(TYPE_2), hasProperty("predicates",
      hasItem(predicate().withName(PROP_I).withIsList(true).withValueTypeCount(2))
    )));
  }

  /* SUBJECT_A has a predicate the points to other subject ex:bar and has a value.
   * SUBJECT_A has a rdf:type TYPE_2. ex:bar has rdf:type bartype.
   *
   * SUBJECT_A should have a list predicate with a value type ex:valuetype and a reference type TYPE_3
   */
  @Test
  public void predicateIsAlsoAListWhenItHasDifferentTypes() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, "value", VALUE_TYPE, null, null, ""),
      CursorQuad.create(SUBJECT_B, RDF_TYPE, OUT, ASSERTED, TYPE_3, null, null, null, "")
    );

    assertThat(schema, hasEntry(is(TYPE_2), hasProperty("predicates", hasItem(
      predicate().withName(PROP_I).withReferenceType(TYPE_3)
                 .withValueType(VALUE_TYPE).withIsList(true)
    ))));
  }

  /* SUBJECT_A has multiple predicates PROP_I pointing to ex:bar and ex:baz and ex:bar has a PROP_I pointing to ex:baz.
   * SUBJECT_A has the rdf:type TYPE_2, ex:bar has the rdf:type TYPE_3 and ex:baz has the rdf:type TYPE_4
   * TYPE_2 should have a list predicate for an outgoing PROP_I
   * TYPE_4 should have a list predicate for an incoming PROP_I
   * TYPE_3 should have a single predicate for an outgoing PROP_I and one for an incoming PROP_I
   */
  @Test
  public void inversePredicatesAreNotAlwaysLists() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_C, null, null, null, "")
    );

    assertThat(schema, allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates",
        hasItem(predicate().withName(PROP_I).withDirection(OUT).withIsList(true))
      )),
      hasEntry(is(RDFS_RESOURCE), hasProperty("predicates",
        hasItem(predicate().withName(PROP_I).withDirection(Direction.IN).withIsList(false))
      ))
    ));
  }

  // add triple1 twice should give hte same result as adding triple1 once
  @Test // TODO figure out how to test in permutation test
  public void doubleAssertionOfATripleDoesNotChangeTheSchema() throws Exception {
    Map<String, Type> singleAssertion = runTest(
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    );

    Map<String, Type> doubleAssertion = runTest(
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    );

    assertThat(doubleAssertion, is(singleAssertion));
  }

  @Test
  public void doubleAssertionOfATripleDoesNotIncreaseTheReferenceCounts() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    );

    assertThat(schema, hasEntry(is(RDFS_RESOURCE), hasProperty("predicates", allOf(
      hasItem(predicate().withName(PROP_I).withDirection(IN).withReferenceTypeCount(1)),
      hasItem(predicate().withName(PROP_I).withDirection(OUT).withReferenceTypeCount(1))
    ))));
  }

  @Test
  public void retractingATripleLeavesTheTypesInTheSchema() throws Exception {
    Map<String, Type> assertRetract = runTest(
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_1, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, RETRACTED, SUBJECT_B, null, null, null, "")
    );

    assertThat(assertRetract, hasEntry(is(TYPE_1), hasProperty("predicates",
      contains(predicate().withName(RDF_TYPE))
    )));
  }

  @Test
  public void retractingATripleInAnotherSessionLeavesTheRetractedPredicateInTheSchema() throws Exception {
    BdbNonPersistentEnvironmentCreator dataStoreFactory = new BdbNonPersistentEnvironmentCreator();
    final BdbSchemaStore schema = new BdbSchemaStore(
      new BdbBackedData(dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "schema",
        false,
        STRING_BINDING,
        STRING_BINDING,
        STRING_IS_CLEAN_HANDLER
      )),
      mock(ImportStatus.class)
    );

    final StoreUpdater storeUpdater = createInstance(dataStoreFactory, schema);

    storeUpdater.start(0, false);
    storeUpdater.onQuad(true, SUBJECT_A, RDF_TYPE, TYPE_1, null, null, null);
    storeUpdater.onQuad(true, SUBJECT_A, PROP_I, SUBJECT_B, null, null, null);
    storeUpdater.commit();
    storeUpdater.start(1, false);
    storeUpdater.onQuad(true, SUBJECT_A, PROP_I, SUBJECT_B, null, null, null);
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), hasEntry(is(TYPE_1), hasProperty("predicates",
      containsInAnyOrder(
        predicate().withName(RDF_TYPE),
        predicate().withName(PROP_I)
      )
    )));
  }

  // add triple1 should be the same as adding triple1, retracting it and adding it again.
  @Test
  public void assertRetractAssertShouldShowNoDifferencesAfterTheFirstAndSecondAssert() throws Exception {
    Map<String, Type> singleAssertion = runTest(
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    );

    Map<String, Type> ara = runTest(
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, RETRACTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    );

    assertThat(ara, is(singleAssertion));
  }

  @Test
  public void onlyRetractingATripleShouldHaveNoInfluenceOnTheSchema() throws Exception {
    Map<String, Type> noChanges = runTest();
    Map<String, Type> retractionOnly = runTest(
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, RETRACTED, SUBJECT_B, null, null, null, "")
    );

    assertThat(retractionOnly, is(noChanges));
  }

  /* When the SUBJECT_A rdf:type TYPE_2 is asserted in a a separate update, while ex:baz did not have a type before.
   * Then I expect the TYPE_2 to get an ex:links predicate that points to ex:type
   */
  @Test
  public void addAValidOutgoingPredicateToTheNewType() throws Exception {
    BdbNonPersistentEnvironmentCreator dataStoreFactory = new BdbNonPersistentEnvironmentCreator();
    final BdbSchemaStore schema = new BdbSchemaStore(
      new BdbBackedData(dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "schema",
        false,
        STRING_BINDING,
        STRING_BINDING,
        STRING_IS_CLEAN_HANDLER
      )),
      mock(ImportStatus.class)
    );

    final StoreUpdater storeUpdater = createInstance(dataStoreFactory, schema);

    storeUpdater.start(0, false);
    storeUpdater.onQuad(true, SUBJECT_A, PROP_III, SUBJECT_C, null, null, null);
    storeUpdater.onQuad(true, SUBJECT_C, RDF_TYPE, TYPE_1, null, null, null);
    storeUpdater.commit();
    storeUpdater.start(1, false);
    storeUpdater.onQuad(true, SUBJECT_A, RDF_TYPE, TYPE_2, null, null, null);
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), hasEntry(is(TYPE_2), hasProperty("predicates",
      hasItem(predicate().withName(PROP_III).withDirection(OUT).withReferenceType(TYPE_1))
    )));
  }

  /* When the SUBJECT_C RDF_TYPE TYPE_1 is asserted in a a separate update, while ex:baz did not have a type before.
   * Then I expect the RDF_TYPE to get an inverse of the ex:links predicate that points to TYPE_2
   *
   * But in the current code that does not happen. Instead ex:type gets an ex:links
   *  predicate that has no reference or value types
   */
  @Test
  public void addAValidIncomingPredicateToTheNewType() throws Exception {
    BdbNonPersistentEnvironmentCreator dataStoreFactory = new BdbNonPersistentEnvironmentCreator();
    final BdbSchemaStore schema = new BdbSchemaStore(
      new BdbBackedData(dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "schema",
        false,
        STRING_BINDING,
        STRING_BINDING,
        STRING_IS_CLEAN_HANDLER
      )),
      mock(ImportStatus.class)
    );

    final StoreUpdater storeUpdater = createInstance(dataStoreFactory, schema);

    storeUpdater.start(0, false);
    storeUpdater.onQuad(true, SUBJECT_A, PROP_III, SUBJECT_C, null, null, null);
    storeUpdater.onQuad(true, SUBJECT_A, RDF_TYPE, TYPE_2, null, null, null);
    storeUpdater.commit();
    storeUpdater.start(1, false);
    storeUpdater.onQuad(true, SUBJECT_C, RDF_TYPE, TYPE_1, null, null, null);
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), hasEntry(is(TYPE_1), hasProperty("predicates",
      hasItem(predicate().withName(PROP_III).withDirection(Direction.IN).withReferenceType(TYPE_2))
    )));
  }

  @Test
  public void addingQuadsWithTheSameSubjectButAnotherGraphShouldCountAsOne() throws Exception {
    BdbNonPersistentEnvironmentCreator dataStoreFactory = new BdbNonPersistentEnvironmentCreator();
    final BdbSchemaStore schema = new BdbSchemaStore(
        new BdbBackedData(dataStoreFactory.getDatabase(
            USER,
            DATA_SET,
            "schema",
            false,
            STRING_BINDING,
            STRING_BINDING,
            STRING_IS_CLEAN_HANDLER
        )),
        mock(ImportStatus.class)
    );

    final StoreUpdater storeUpdater = createInstance(dataStoreFactory, schema);

    storeUpdater.start(0, false);
    storeUpdater.onQuad(true, SUBJECT_A, RDF_TYPE, TYPE_1, null, null, GRAPH_1);
    storeUpdater.onQuad(true, SUBJECT_B, RDF_TYPE, TYPE_1, null, null, GRAPH_1);
    storeUpdater.onQuad(true, SUBJECT_A, RDF_TYPE, TYPE_1, null, null, GRAPH_2);
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), hasEntry(is(TYPE_1), hasProperty("subjectsWithThisType", is(2L))));
  }

  @Test
  public void addingQuadsWithTheSameSubjectButAnotherGraphShouldCountAsUnchanged() throws Exception {
    BdbNonPersistentEnvironmentCreator dataStoreFactory = new BdbNonPersistentEnvironmentCreator();
    final BdbSchemaStore schema = new BdbSchemaStore(
        new BdbBackedData(dataStoreFactory.getDatabase(
            USER,
            DATA_SET,
            "schema",
            false,
            STRING_BINDING,
            STRING_BINDING,
            STRING_IS_CLEAN_HANDLER
        )),
        mock(ImportStatus.class)
    );

    final StoreUpdater storeUpdater = createInstance(dataStoreFactory, schema);

    storeUpdater.start(0, false);
    storeUpdater.onQuad(true, SUBJECT_A, RDF_TYPE, TYPE_1, null, null, GRAPH_1);
    storeUpdater.onQuad(true, SUBJECT_B, RDF_TYPE, TYPE_1, null, null, GRAPH_1);
    storeUpdater.commit();

    storeUpdater.start(1, false);
    storeUpdater.onQuad(true, SUBJECT_C, RDF_TYPE, TYPE_1, null, null, GRAPH_2);
    storeUpdater.onQuad(true, SUBJECT_A, RDF_TYPE, TYPE_1, null, null, GRAPH_2);
    storeUpdater.onQuad(true, SUBJECT_B, RDF_TYPE, TYPE_1, null, null, GRAPH_2);
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), hasEntry(is(TYPE_1), hasProperty("subjectsWithThisType", is(3L))));
  }

  @Test
  public void removingQuadsWithTheSameSubjectButAnotherGraphShouldCountAsUnchanged() throws Exception {
    BdbNonPersistentEnvironmentCreator dataStoreFactory = new BdbNonPersistentEnvironmentCreator();
    final BdbSchemaStore schema = new BdbSchemaStore(
        new BdbBackedData(dataStoreFactory.getDatabase(
            USER,
            DATA_SET,
            "schema",
            false,
            STRING_BINDING,
            STRING_BINDING,
            STRING_IS_CLEAN_HANDLER
        )),
        mock(ImportStatus.class)
    );

    final StoreUpdater storeUpdater = createInstance(dataStoreFactory, schema);

    storeUpdater.start(0, false);
    storeUpdater.onQuad(true, SUBJECT_A, RDF_TYPE, TYPE_1, null, null, GRAPH_1);
    storeUpdater.onQuad(true, SUBJECT_B, RDF_TYPE, TYPE_1, null, null, GRAPH_1);
    storeUpdater.onQuad(true, SUBJECT_B, RDF_TYPE, TYPE_1, null, null, GRAPH_2);
    storeUpdater.onQuad(true, SUBJECT_C, RDF_TYPE, TYPE_1, null, null, GRAPH_2);
    storeUpdater.commit();

    storeUpdater.start(1, false);
    storeUpdater.onQuad(false, SUBJECT_B, RDF_TYPE, TYPE_1, null, null, GRAPH_2);
    storeUpdater.onQuad(false, SUBJECT_C, RDF_TYPE, TYPE_1, null, null, GRAPH_2);
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), hasEntry(is(TYPE_1), hasProperty("subjectsWithThisType", is(2L))));
  }

  private Map<String, Type> runTest(CursorQuad... quads) throws Exception {
    BdbNonPersistentEnvironmentCreator dataStoreFactory = new BdbNonPersistentEnvironmentCreator();

    final BdbSchemaStore schema = new BdbSchemaStore(
      new BdbBackedData(dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "schema",
        false,
        STRING_BINDING,
        STRING_BINDING,
        STRING_IS_CLEAN_HANDLER
      )),
      mock(ImportStatus.class)
    );

    final StoreUpdater storeUpdater = createInstance(dataStoreFactory, schema);

    storeUpdater.start(0, false);
    for (CursorQuad quad : quads) {
      storeUpdater.onQuad(
        quad.getChangeType() == ASSERTED,
        quad.getSubject(),
        quad.getPredicate(),
        quad.getObject(),
        quad.getValuetype().orElse(null),
        quad.getLanguage().orElse(null),
        quad.getGraph().orElse(null)
      );
    }
    storeUpdater.commit();

    return schema.getStableTypes();
  }

  private StoreUpdater createInstance(BdbNonPersistentEnvironmentCreator dataStoreFactory, BdbSchemaStore schema)
    throws DataStoreCreationException, BdbDbCreationException,
    IOException {
    final BdbQuadStore quadStore = new BdbQuadStore(dataStoreFactory.getDatabase(
      USER,
      DATA_SET,
      "rdfData",
      true,
      STRING_BINDING,
      STRING_BINDING,
      STRING_IS_CLEAN_HANDLER
    ));

    final GraphStore graphStore = new GraphStore(
        dataStoreFactory.getDatabase(
            USER,
            DATA_SET,
            "graphStore",
            true,
            STRING_BINDING,
            STRING_BINDING,
            STRING_IS_CLEAN_HANDLER
        )
    );

    final DefaultResourcesStore defaultResourcesStore = new DefaultResourcesStore(
        dataStoreFactory.getDatabase(
            USER,
            DATA_SET,
            "defaultResourcesStore",
            true,
            STRING_BINDING,
            STRING_BINDING,
            STRING_IS_CLEAN_HANDLER
        ),
        mock(ImportStatus.class)
    );

    final BdbTypeNameStore typeNameStore = new BdbTypeNameStore(
      new BdbBackedData(dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "typenames",
        false,
        STRING_BINDING,
        STRING_BINDING,
        STRING_IS_CLEAN_HANDLER
      )),
        GRAPH_1
    );

    final UpdatedPerPatchStore updatedPerPatchStore = new UpdatedPerPatchStore(
      dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "updatedPerPatch",
        true,
        STRING_BINDING,
        INT_BINDING,
        STRING_INT_IS_CLEAN_HANDLER
      )
    );

    final BdbPatchVersionStore patchVersionStore = new BdbPatchVersionStore(
        dataStoreFactory.getDatabase(
            USER,
            DATA_SET,
            "patchVersion",
            true,
            STRING_BINDING,
            STRING_BINDING,
            STRING_IS_CLEAN_HANDLER
        )
    );

    final BdbRmlDataSourceStore rmlDataSourceStore = new BdbRmlDataSourceStore(
      dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "rmlSource",
        true,
        STRING_BINDING,
        STRING_BINDING,
        STRING_IS_CLEAN_HANDLER
      ),
      mock(ImportStatus.class)
    );

    final OldSubjectTypesStore oldSubjectTypesStore = new OldSubjectTypesStore(dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "oldSubjectTypes",
        true,
        STRING_BINDING,
        STRING_BINDING,
        STRING_IS_CLEAN_HANDLER
    ));

    ChangeLogStorage changeLogStorage = mock(ChangeLogStorage.class);
    given(changeLogStorage.getChangeLogOutputStream(anyInt())).will(invocation -> OutputStream.nullOutputStream());

    return new StoreUpdater(
      quadStore,
      graphStore,
      typeNameStore,
      patchVersionStore,
      updatedPerPatchStore,
      oldSubjectTypesStore,
      Lists.newArrayList(schema, rmlDataSourceStore, defaultResourcesStore),
      mock(ImportStatus.class),
      changeLogStorage
    );
  }
}
