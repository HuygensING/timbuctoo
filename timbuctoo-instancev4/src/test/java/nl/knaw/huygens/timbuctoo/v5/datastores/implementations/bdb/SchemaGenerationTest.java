package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.google.common.collect.Lists;
import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.IsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbNonPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.PredicateMatcher.predicate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class SchemaGenerationTest {

  private static final String USER = "user";
  private static final String DATA_SET = "dataSet";
  private static final StringStringIsCleanHandler STRING_IS_CLEAN_HANDLER = new StringStringIsCleanHandler();
  private static final TupleBinding<String> STRING_BINDING = TupleBinding.getPrimitiveBinding(String.class);


  @Test
  public void everyTypeOfTheSubjectsIsAddedToTheSchema() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/type", null, null, ""),
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/footype", null, null, ""),
      CursorQuad.create("http://example.org/bar", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/barType", null, null, "")
    );
    assertThat(schema, allOf(
      hasEntry(is("http://example.org/type"), hasProperty("name", is("http://example.org/type"))),
      hasEntry(is("http://example.org/type"), hasProperty("name", is("http://example.org/type"))),
      hasEntry(is("http://example.org/barType"), hasProperty("name", is("http://example.org/barType")))
    ));
  }

  /* Every "normal" (non rdf:type) predicate of ex:foo (ex:pred1, ex:pred1) will be added to ex:type and ex:footype
   */
  @Test
  public void predicateOfASubjectIsAddedToEachType() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/type", null, null, ""),
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/footype", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/bar", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred2", Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/bar2", null, null, "")
    );

    assertThat(schema, allOf(
      hasEntry(is("http://example.org/type"), hasProperty("predicates",allOf(
        hasItem(predicate().withName("http://example.org/pred1")),
        hasItem(predicate().withName("http://example.org/pred2"))
      ))),
      hasEntry(is("http://example.org/type"), hasProperty("predicates", allOf(
        hasItem(predicate().withName("http://example.org/pred1")),
        hasItem(predicate().withName("http://example.org/pred2"))
      )))
    ));
  }

  /* ex:pred1 of ex:foo should be added to ex:footype as an outgoing predicate with a reference type ex:bartype
   * ex:pred1 of ex:foo should be added to ex:bartype as an incoming predicate with a reference type ex:footype
   */
  @Test
  public void eachPredicateThatLinksToAnotherSubjectWillBeAddedToTheOtherSubjectAsAnIncoming() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/footype", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/bar", null, null, ""),
      CursorQuad.create("http://example.org/bar", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/bartype", null, null, "")

    );

    assertThat(schema, allOf(
      hasEntry(is("http://example.org/footype"), hasProperty("predicates",allOf(
        hasItem(predicate().withName("http://example.org/pred1").withDirection(Direction.OUT).withReferenceType("http://example.org/bartype"))
      ))),
      hasEntry(is("http://example.org/bartype"), hasProperty("predicates", allOf(
        hasItem(predicate().withName("http://example.org/pred1").withDirection(Direction.IN).withReferenceType("http://example.org/footype"))
      )))
    ));
  }

  /* ex:pred1 of ex:foo should be added to ex:footype as an outgoing predicate with a reference type tim:unknown
   * ex:pred1 of ex:foo should be added to tim:unknown as an incoming predicate with a reference type ex:footype
   */
  @Test
  public void ifTheReferencedSubjectHasNoTypeThePredicateWillBeAddedToTimUnknown() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/footype", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/bar", null, null, "")
    );

    assertThat(schema, allOf(
      hasEntry(is("http://example.org/footype"), hasProperty("predicates",allOf(
        hasItem(predicate().withName("http://example.org/pred1").withDirection(Direction.OUT).withReferenceType(RdfConstants.UNKNOWN))
      ))),
      hasEntry(is(RdfConstants.UNKNOWN), hasProperty("predicates", allOf(
        hasItem(predicate().withName("http://example.org/pred1").withDirection(Direction.IN).withReferenceType("http://example.org/footype"))
      )))
    ));
  }

  /* Predicate ex:pred1 of ex:foo has value type ex:valuetype and ex:foo has rdf:type ex:footype.
   * The schema should have an entry for ex:footype and
   * that entry should have a predicate for ex:pred1 with valuetype ex:valuetype.
   */
  @Test
  public void theValueTypeIsAddedToThePredicate() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/footype", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "value", "http://example.org/valuetype", null, "")
    );
    assertThat(schema, hasEntry(is("http://example.org/footype"), hasProperty("predicates",
      hasItem(predicate().withName("http://example.org/pred1").withValueType("http://example.org/valuetype"))
    )));
  }

  /* ex:foo has multiple predicate ex:pred1 pointing to multiple values
   * This should make predicate ex:pred1 a list predicate
   */
  @Test
  public void thePredicateWillBecomeAListWhenASubjectHasMultipleInstances() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/footype", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "value", "http://example.org/valuetype", null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "value2", "http://example.org/valuetype", null, "")
    );
    assertThat(schema, hasEntry(is("http://example.org/footype"), hasProperty("predicates",
      hasItem(predicate().withName("http://example.org/pred1").withIsList(true))
    )));
  }

  /* ex:foo has a predicate the points to other subject ex:bar and has a value.
   * ex:foo has a rdf:type ex:footype. ex:bar has rdf:type bartype.
   *
   * ex:foo should have a list predicate with a value type ex:valuetype and a reference type ex:bartype
   */
  @Test
  public void predicateIsAlsoAListWhenItHasDifferentTypes() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/footype", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/bar", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "value", "http://example.org/valuetype", null, ""),
      CursorQuad.create("http://example.org/bar", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/bartype", null, null, "")
    );

    assertThat(schema, hasEntry(is("http://example.org/footype"), hasProperty("predicates", hasItem(
      predicate().withName("http://example.org/pred1").withReferenceType("http://example.org/bartype")
                 .withValueType("http://example.org/valuetype").withIsList(true)
    ))));
  }

  /* ex:foo has multiple predicates ex:pred1 pointing to ex:bar and ex:baz and ex:bar has a ex:pred1 pointing to ex:baz.
   * ex:foo has the rdf:type ex:footype, ex:bar has the rdf:type ex:bartype and ex:baz has the rdf:type ex:baztype
   * ex:footype should have a list predicate for an outgoing ex:pred1
   * ex:baztype should have a list predicate for an incoming ex:pred1
   * ex:bartype should have a single predicate for an outgoing ex:pred1 and one for an incoming ex:pred1
   */
  @Test
  public void inversePredicatesAreNotAlwaysLists() throws Exception {
    Map<String, Type> schema = runTest(
      CursorQuad.create("http://example.org/foo", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/footype", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/bar", null, null, ""),
      CursorQuad.create("http://example.org/foo", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/baz", null, null, ""),
      CursorQuad.create("http://example.org/bar", "http://example.org/pred1", Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/baz", null, null, ""),
      CursorQuad.create("http://example.org/bar", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/bartype", null, null, ""),
      CursorQuad.create("http://example.org/baz", RdfConstants.RDF_TYPE, Direction.OUT, ChangeType.ASSERTED,
        "http://example.org/baztype", null, null, "")
    );

    assertThat(schema,
      hasEntry(is("http://example.org/footype"), hasProperty("predicates",
        hasItem(predicate().withName("http://example.org/pred1").withDirection(Direction.OUT).withIsList(true))
      ))
    );
    assertThat(schema,
      hasEntry(is("http://example.org/bartype"), hasProperty("predicates", allOf(
        hasItem(predicate().withName("http://example.org/pred1").withDirection(Direction.OUT).withIsList(false)),
        hasItem(predicate().withName("http://example.org/pred1").withDirection(Direction.IN).withIsList(false))
      )))
    );
    assertThat(schema,
      hasEntry(is("http://example.org/baztype"), hasProperty("predicates",
        hasItem(predicate().withName("http://example.org/pred1").withDirection(Direction.IN).withIsList(true))
      ))
    );
  }


  /* When the ex:foo rdf:type ex:footype is asserted in a a separate update, while ex:baz did not have a type before.
   * Then I expect the ex:footype to get an ex:links predicate that points to ex:type
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

    storeUpdater.start(0);
    storeUpdater.onQuad(true, "http://example.org/foo", "http://example.org/links", "http://example.org/baz", null, null, "http://example.org");
    storeUpdater.onQuad(true, "http://example.org/baz", RdfConstants.RDF_TYPE, "http://example.org/type", null, null, "http://example.org");
    storeUpdater.commit();
    storeUpdater.start(1);
    storeUpdater.onQuad(true, "http://example.org/foo", RdfConstants.RDF_TYPE, "http://example.org/footype", null, null, "http://example.org");
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), hasEntry(is("http://example.org/footype"), hasProperty("predicates",
      hasItem(
        predicate()
          .withName("http://example.org/links")
          .withDirection(Direction.OUT)
          .withReferenceType("http://example.org/type")
      )
    )));
  }

  /* When the ex:baz rdf:type ex:type is asserted in a a separate update, while ex:baz did not have a type before.
   * Then I expect the ex:type to get an inverse of the ex:links predicate that points to ex:fooType
   *
   * But in the current code that does not happen. Instead ex:type gets an ex:links
   * predicate that has no reference or value types
   */
  @Ignore
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

    storeUpdater.start(0);
    storeUpdater.onQuad(true, "http://example.org/foo", "http://example.org/links", "http://example.org/baz", null, null, "http://example.org");
    storeUpdater.onQuad(true, "http://example.org/foo", RdfConstants.RDF_TYPE, "http://example.org/footype", null, null, "http://example.org");
    storeUpdater.commit();
    storeUpdater.start(1);
    storeUpdater.onQuad(true, "http://example.org/baz", RdfConstants.RDF_TYPE, "http://example.org/type", null, null, "http://example.org");
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), hasEntry(is("http://example.org/type"), hasProperty("predicates",
      hasItem(
        predicate()
          .withName("http://example.org/links")
          .withDirection(Direction.IN)
          .withReferenceType("http://example.org/footype")
      )
    )));
  }

  /* When the ex:baz rdf:type ex:type is asserted in a a separate update, while ex:baz did not have a type before.
   * Then I expect the ex:footypes ex:links will have the reference type ex:type added
   *
   * But in the current code that does not happen.
   */
  @Ignore
  @Test
  public void updateTheInversePredicatesForReferencePredicates() throws Exception {
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

    storeUpdater.start(0);
    storeUpdater.onQuad(true, "http://example.org/foo", "http://example.org/links", "http://example.org/baz", null, null, "http://example.org");
    storeUpdater.onQuad(true, "http://example.org/foo", RdfConstants.RDF_TYPE, "http://example.org/footype", null, null, "http://example.org");
    storeUpdater.commit();
    storeUpdater.start(1);
    storeUpdater.onQuad(true, "http://example.org/baz", RdfConstants.RDF_TYPE, "http://example.org/type", null, null, "http://example.org");
    storeUpdater.commit();

    assertThat(schema.getStableTypes(), hasEntry(is("http://example.org/footype"), hasProperty("predicates",
      hasItem(
        predicate()
          .withName("http://example.org/links")
          .withDirection(Direction.OUT)
          .withReferenceType("http://example.org/type")
      )
    )));
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

    storeUpdater.start(0);
    for (CursorQuad quad : quads) {
      storeUpdater.onQuad(true, quad.getSubject(), quad.getPredicate(), quad.getObject(), quad.getValuetype().orElse(null), quad.getLanguage().orElse(null), "http://example.org");
    }
    storeUpdater.commit();

    return schema.getStableTypes();
  }

  private StoreUpdater createInstance(BdbNonPersistentEnvironmentCreator dataStoreFactory, BdbSchemaStore schema)
    throws DataStoreCreationException, nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.BdbDbCreationException,
    IOException {
    BdbTripleStore quadStore = new BdbTripleStore(dataStoreFactory.getDatabase(
      USER,
      DATA_SET,
      "rdfData",
      true,
      STRING_BINDING,
      STRING_BINDING,
      STRING_IS_CLEAN_HANDLER
    ));
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
      "http://example.org"
    );

    final BdbTruePatchStore truePatchStore = new BdbTruePatchStore(
      dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "truePatch",
        true,
        STRING_BINDING,
        STRING_BINDING,
        STRING_IS_CLEAN_HANDLER
      )
    );
    final TupleBinding<Integer> integerBinding = TupleBinding.getPrimitiveBinding(Integer.class);
    final UpdatedPerPatchStore updatedPerPatchStore = new UpdatedPerPatchStore(
      dataStoreFactory.getDatabase(
        USER,
        DATA_SET,
        "updatedPerPatch",
        true,
        integerBinding,
        STRING_BINDING,
        new IsCleanHandler<Integer, String>() {
          @Override
          public Integer getKey() {
            return Integer.MAX_VALUE;
          }

          @Override
          public String getValue() {
            return "isClean";
          }
        }
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
    VersionStore versionStore = new VersionStore(dataStoreFactory.getDatabase(
      USER,
      DATA_SET,
      "versions",
      false,
      STRING_BINDING,
      integerBinding,
      new IsCleanHandler<String, Integer>() {
        @Override
        public String getKey() {
          return "isClean";
        }

        @Override
        public Integer getValue() {
          return Integer.MAX_VALUE;
        }
      }
    ));

    return new StoreUpdater(
      dataStoreFactory,
      quadStore,
      typeNameStore,
      truePatchStore,
      updatedPerPatchStore,
      Lists.newArrayList(schema, rmlDataSourceStore),
      versionStore,
      mock(ImportStatus.class)
    );
  }
}
