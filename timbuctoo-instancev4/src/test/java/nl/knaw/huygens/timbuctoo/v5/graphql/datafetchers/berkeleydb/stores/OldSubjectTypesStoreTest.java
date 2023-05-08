package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.stores;

import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.OldSubjectTypesStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore.SubjectCursor;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbNonPersistentEnvironmentCreator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class OldSubjectTypesStoreTest {
  public static final String EX = "http://example.org/";
  public static final String PRED = "http://pred/";
  protected BdbNonPersistentEnvironmentCreator databaseCreator;
  protected OldSubjectTypesStore oldSubjectTypesStore;

  @BeforeEach
  public void makeCollection() throws Exception {
    databaseCreator = new BdbNonPersistentEnvironmentCreator();
    oldSubjectTypesStore = new OldSubjectTypesStore(
        databaseCreator.getDatabase(
            "userId",
            "dataSetId",
            "oldSubjectTypes",
            true,
            TupleBinding.getPrimitiveBinding(String.class),
            TupleBinding.getPrimitiveBinding(String.class),
            new StringStringIsCleanHandler()
        )
    );
    Thread.sleep(2000); // to make the test work on slow systems
  }

  @AfterEach
  public void cleanUp() throws Exception {
    databaseCreator.close();
  }

  @Test
  public void returnsTheData() throws Exception {
    oldSubjectTypesStore.put(EX + "subject1", PRED + "a", 0);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "a", 0);
    oldSubjectTypesStore.put(EX + "subject4", PRED + "a", 1);
    oldSubjectTypesStore.put(EX + "subject5", PRED + "a", 1);
    oldSubjectTypesStore.put(EX + "subject6", PRED + "a", 2);
    oldSubjectTypesStore.put(EX + "subject1", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject3", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject1", PRED + "c", 1);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "c", 1);

    try (Stream<SubjectCursor> subjects = oldSubjectTypesStore.fromTypeAndVersion(PRED + "a", 0, "")) {
      List<SubjectCursor> resultList = subjects.collect(toList());
      assertThat(resultList, contains(
          SubjectCursor.create(EX + "subject1", Set.of(0), "0\n" + EX + "subject1"),
          SubjectCursor.create(EX + "subject2", Set.of(0), "0\n" + EX + "subject2"),
          SubjectCursor.create(EX + "subject4", Set.of(1), "1\n" + EX + "subject4"),
          SubjectCursor.create(EX + "subject5", Set.of(1), "1\n" + EX + "subject5"),
          SubjectCursor.create(EX + "subject6", Set.of(2), "2\n" + EX + "subject6")
      ));
    }
  }

  @Test
  public void canIterateFromCursor() throws Exception {
    oldSubjectTypesStore.put(EX + "subject1", PRED + "a", 0);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "a", 0);
    oldSubjectTypesStore.put(EX + "subject4", PRED + "a", 1);
    oldSubjectTypesStore.put(EX + "subject5", PRED + "a", 1);
    oldSubjectTypesStore.put(EX + "subject6", PRED + "a", 2);
    oldSubjectTypesStore.put(EX + "subject1", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject3", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject1", PRED + "c", 1);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "c", 1);

    String cursor;
    try (Stream<SubjectCursor> subjects = oldSubjectTypesStore.fromTypeAndVersion(PRED + "a", 0, "")) {
      // Get the first two items and the cursor of the last one
      cursor = subjects.limit(2).reduce((first, second) -> second).orElse(null).getCursor();
    }

    try (Stream<SubjectCursor> subjects = oldSubjectTypesStore.fromTypeAndVersion(PRED + "a", 0, "A\n" + cursor)) {
      List<SubjectCursor> resultList = subjects.collect(toList());
      assertThat(resultList, contains(
          // Starting from the cursor and going ascending should give us the last two
          SubjectCursor.create(EX + "subject4", Set.of(1), "1\n" + EX + "subject4"),
          SubjectCursor.create(EX + "subject5", Set.of(1), "1\n" + EX + "subject5"),
          SubjectCursor.create(EX + "subject6", Set.of(2), "2\n" + EX + "subject6")
      ));
    }
  }

  @Test
  public void canIterateFromSpecificVersion() throws Exception {
    oldSubjectTypesStore.put(EX + "subject1", PRED + "a", 0);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "a", 0);
    oldSubjectTypesStore.put(EX + "subject4", PRED + "a", 1);
    oldSubjectTypesStore.put(EX + "subject5", PRED + "a", 1);
    oldSubjectTypesStore.put(EX + "subject6", PRED + "a", 2);
    oldSubjectTypesStore.put(EX + "subject1", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject3", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject1", PRED + "c", 1);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "c", 1);

    try (Stream<SubjectCursor> subjects = oldSubjectTypesStore.fromTypeAndVersion(PRED + "a", 1, "")) {
      List<SubjectCursor> resultList = subjects.collect(toList());
      assertThat(resultList, contains(
          SubjectCursor.create(EX + "subject4", Set.of(1), "1\n" + EX + "subject4"),
          SubjectCursor.create(EX + "subject5", Set.of(1), "1\n" + EX + "subject5"),
          SubjectCursor.create(EX + "subject6", Set.of(2), "2\n" + EX + "subject6")
      ));
    }
  }

  @Test
  public void canIterateFromSpecificVersionAndCursor() throws Exception {
    oldSubjectTypesStore.put(EX + "subject1", PRED + "a", 0);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "a", 0);
    oldSubjectTypesStore.put(EX + "subject4", PRED + "a", 1);
    oldSubjectTypesStore.put(EX + "subject5", PRED + "a", 1);
    oldSubjectTypesStore.put(EX + "subject6", PRED + "a", 2);
    oldSubjectTypesStore.put(EX + "subject1", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject3", PRED + "b", 0);
    oldSubjectTypesStore.put(EX + "subject1", PRED + "c", 1);
    oldSubjectTypesStore.put(EX + "subject2", PRED + "c", 1);

    String cursor;
    try (Stream<SubjectCursor> subjects = oldSubjectTypesStore.fromTypeAndVersion(PRED + "a", 1, "")) {
      // Get the first two items and the cursor of the last one
      cursor = subjects.limit(2).reduce((first, second) -> second).orElse(null).getCursor();
    }

    try (Stream<SubjectCursor> subjects = oldSubjectTypesStore.fromTypeAndVersion(PRED + "a", 1, "A\n" + cursor)) {
      List<SubjectCursor> resultList = subjects.collect(toList());
      assertThat(resultList, contains(
          // Starting from the cursor and going ascending should give us the last two
          SubjectCursor.create(EX + "subject6", Set.of(2), "2\n" + EX + "subject6")
      ));
    }
  }
}
