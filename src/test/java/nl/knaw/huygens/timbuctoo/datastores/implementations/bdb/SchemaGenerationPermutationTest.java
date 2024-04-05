package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import com.google.common.collect.Lists;
import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.util.ListPartitionerTest;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringIntegerIsCleanHandler;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.Type;
import nl.knaw.huygens.timbuctoo.dropwizard.BdbNonPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.filestorage.ChangeLogStorage;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Collections2.orderedPermutations;
import static java.util.Arrays.asList;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.ChangeType.ASSERTED;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.ChangeType.RETRACTED;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction.IN;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction.OUT;
import static nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.PredicateMatcher.predicate;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.util.RdfConstants.RDFS_RESOURCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Disabled // disable test because our docker hub build fails with this test on
public class SchemaGenerationPermutationTest {
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

  private static final String PROP_I = "http://example.org/pred1";
  private static final String PROP_II = "http://example.org/pred2";

  private static final String GRAPH = "http://example.org";

  // Run the cases one by one
  // Some test generate a large amount of permutations
  public static Stream<Object[]> getData() {
    List<Object[]> testCases = Lists.newArrayList();
    // predicateOfASubjectIsAddedToEachType
    testCases.addAll(createPermutationsOfTestCase(
      allOf(
        hasEntry(is(TYPE_1), hasProperty("name", is(TYPE_1))),
        hasEntry(is(TYPE_1), hasProperty("name", is(TYPE_1))),
        hasEntry(is(TYPE_3), hasProperty("name", is(TYPE_3)))
      ),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_1, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_B, RDF_TYPE, OUT, ASSERTED, TYPE_3, null, null, null, "")
    ));
    // predicateOfASubjectIsAddedToEachType
    testCases.addAll(createPermutationsOfTestCase(
      allOf(
        hasEntry(is(TYPE_1), hasProperty("predicates", allOf(
          hasItem(predicate().withName(PROP_I)),
          hasItem(predicate().withName(PROP_II))
        ))),
        hasEntry(is(TYPE_2), hasProperty("predicates", allOf(
          hasItem(predicate().withName(PROP_I)),
          hasItem(predicate().withName(PROP_II))
        )))
      ),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_1, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_II, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    ));
    // eachPredicateThatLinksToAnotherSubjectWillBeAddedToTheOtherSubjectAsAnIncoming
    testCases.addAll(createPermutationsOfTestCase(allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I).withDirection(OUT).withReferenceType(TYPE_3))
      ))),
      hasEntry(is(TYPE_3), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I).withDirection(IN).withReferenceType(TYPE_2))
      )))
      ),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_B, RDF_TYPE, OUT, ASSERTED, TYPE_3, null, null, null, "")
    ));
    testCases.addAll(createPermutationsOfTestCase(allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates",allOf(
        hasItem(predicate().withName(PROP_I).withDirection(OUT).withReferenceType(TYPE_3))
      ))),
      hasEntry(is(TYPE_3), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I).withDirection(IN).withReferenceType(TYPE_2))
      )))
    ),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_B, RDF_TYPE, OUT, ASSERTED, TYPE_3, null, null, null, "")
    ));
    // ifTheReferencedSubjectHasNoTypeThePredicateWillBeAddedToTimUnknown
    testCases.addAll(createPermutationsOfTestCase(allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I).withDirection(OUT).withReferenceType(RDFS_RESOURCE))
      ))),
      hasEntry(is(RDFS_RESOURCE), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I).withDirection(IN).withReferenceType(TYPE_2))
      )))
      ),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    ));
    // theValueTypeIsAddedToThePredicate
    testCases.addAll(createPermutationsOfTestCase(allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates",
        hasItem(predicate().withName(PROP_I).withValueType("http://example.org/valuetype"))
      ))),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, "value", "http://example.org/valuetype", null, null, "")
    ));
    // thePredicateWillBecomeAListWhenASubjectHasMultipleInstances
    testCases.addAll(createPermutationsOfTestCase(allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates",
        hasItem(predicate().withName(PROP_I).withIsList(true).withValueTypeCount(2))
      ))),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, "value", "http://example.org/valuetype", null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, "value2", "http://example.org/valuetype", null, null, "")
    ));
    // predicateIsAlsoAListWhenItHasDifferentTypes
    testCases.addAll(createPermutationsOfTestCase(
      allOf(hasEntry(is(TYPE_2), hasProperty("predicates", hasItem(
        predicate().withName(PROP_I).withReferenceType(TYPE_3)
                   .withValueType("http://example.org/valuetype").withIsList(true)
      )))),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, "value", "http://example.org/valuetype", null, null, ""),
      CursorQuad.create(SUBJECT_B, RDF_TYPE, OUT, ASSERTED, TYPE_3, null, null, null, "")
    ));
    // inversePredicatesAreNotAlwaysLists
    testCases.addAll(createPermutationsOfTestCase(allOf(
      hasEntry(is(TYPE_2), hasProperty("predicates",
        hasItem(predicate().withName(PROP_I).withDirection(OUT).withIsList(true))
      )),
      hasEntry(is(RDFS_RESOURCE), hasProperty("predicates",
        hasItem(predicate().withName(PROP_I).withDirection(IN).withIsList(false))
      ))),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_2, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_C, null, null, null, "")
    ));
    // aDoubleAssertionOfATripleDoesNotIncreaseTheReferenceCounts
    testCases.addAll(createPermutationsOfTestCase(
      allOf(hasEntry(is(RDFS_RESOURCE), hasProperty("predicates", allOf(
        hasItem(predicate().withName(PROP_I).withDirection(IN).withReferenceTypeCount(1)),
        hasItem(predicate().withName(PROP_I).withDirection(OUT).withReferenceTypeCount(1))
      )))),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, "")
    ));
    // retractingATripleLeavesTheTypesInTheSchema
    // TODO fix tests? Schema ignores predicates that are asserted and retracted in one session
    testCases.addAll(createPartitionsOfTestCase( // use partitions, because the case contains a retraction
      allOf(
        hasEntry(is(TYPE_1), hasProperty("name", is(TYPE_1)))
      ),
      CursorQuad.create(SUBJECT_A, RDF_TYPE, OUT, ASSERTED, TYPE_1, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, ASSERTED, SUBJECT_B, null, null, null, ""),
      CursorQuad.create(SUBJECT_A, PROP_I, OUT, RETRACTED, SUBJECT_B, null, null, null, "")
    ));
    return testCases.stream();
  }

  private static List<Object[]> createPartitionsOfTestCase(Matcher<Map<String, Type>> result,
                                                           CursorQuad... quads) {
    List<Object[]> permutations = Lists.newArrayList();

    List<List<List<CursorQuad>>> partitions = ListPartitionerTest.partition(Lists.newArrayList(quads));
    for (List<List<CursorQuad>> partition : partitions) {
      permutations.add(new Object[]{partition, result});
    }

    return permutations;
  }

  private static List<Object[]> createPermutationsOfTestCase(Matcher<Map<String, Type>> result,
                                                             CursorQuad... quads) {
    List<Object[]> permutations = Lists.newArrayList();

    Collection<List<CursorQuad>> quadPerms = orderedPermutations(asList(quads), new CursorQuadComparator());
    quadPerms.forEach(quadPerm -> {
      List<List<List<CursorQuad>>> partitions = ListPartitionerTest.partition(quadPerm);
      for (List<List<CursorQuad>> partition : partitions) {
        permutations.add(new Object[]{partition, result});
      }
    });

    return permutations;
  }

  @ParameterizedTest
  @MethodSource("getData")
  public void runTest(List<List<CursorQuad>> input, Matcher<Map<String, Type>> result) throws Exception {
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

    try {
      final StoreUpdater storeUpdater = createInstance(dataStoreFactory, schema);

      for (List<CursorQuad> cursorQuadList : input) {
        storeUpdater.start(storeUpdater.getCurrentVersion() + 1);
        for (CursorQuad quad : cursorQuadList) {
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
      }

      assertThat(schema.getStableTypes(), result);
    } finally {
      dataStoreFactory.close();
    }
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
      GRAPH
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
    given(changeLogStorage.getChangeLogOutputStream(anyInt())).willAnswer(inv -> OutputStream.nullOutputStream());

    return new StoreUpdater(
      quadStore,
      graphStore,
      typeNameStore,
      patchVersionStore,
      updatedPerPatchStore,
      oldSubjectTypesStore,
      Lists.newArrayList(schema, defaultResourcesStore),
      mock(ImportStatus.class),
      changeLogStorage
    );
  }

  private static class CursorQuadComparator implements Comparator<CursorQuad> {
    @Override
    public int compare(CursorQuad o1, CursorQuad o2) {
      return o1.getSubject().compareTo(o2.getSubject()) +
        o1.getPredicate().compareTo(o2.getPredicate()) +
        o1.getObject().compareTo(o2.getObject()) +
        o1.getDirection().compareTo(o2.getDirection());
    }
  }
}
