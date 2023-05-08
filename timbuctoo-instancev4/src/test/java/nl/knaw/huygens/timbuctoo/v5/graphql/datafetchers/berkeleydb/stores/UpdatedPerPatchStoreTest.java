package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.stores;

import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.StringIntegerIsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.UpdatedPerPatchStore;
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

public class UpdatedPerPatchStoreTest {
  public static final String EX = "http://example.org/";
  protected BdbNonPersistentEnvironmentCreator databaseCreator;
  protected UpdatedPerPatchStore updatedPerPatchStore;

  @BeforeEach
  public void makeCollection() throws Exception {
    databaseCreator = new BdbNonPersistentEnvironmentCreator();
    updatedPerPatchStore = new UpdatedPerPatchStore(
        databaseCreator.getDatabase(
            "userId",
            "dataSetId",
            "updatedPerPatch",
            true,
            TupleBinding.getPrimitiveBinding(String.class),
            TupleBinding.getPrimitiveBinding(Integer.class),
            new StringIntegerIsCleanHandler()
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
    updatedPerPatchStore.put(0, EX + "subject1");
    updatedPerPatchStore.put(0, EX + "subject2");
    updatedPerPatchStore.put(0, EX + "subject3");
    updatedPerPatchStore.put(1, EX + "subject1");
    updatedPerPatchStore.put(1, EX + "subject3");
    updatedPerPatchStore.put(1, EX + "subject4");
    updatedPerPatchStore.put(2, EX + "subject3");
    updatedPerPatchStore.put(2, EX + "subject4");

    try (Stream<SubjectCursor> subjects = updatedPerPatchStore.fromVersion(0, "")) {
      List<SubjectCursor> resultList = subjects.collect(toList());
      assertThat(resultList, contains(
          SubjectCursor.create(EX + "subject1", Set.of(0, 1)),
          SubjectCursor.create(EX + "subject2", Set.of(0)),
          SubjectCursor.create(EX + "subject3", Set.of(0, 1, 2)),
          SubjectCursor.create(EX + "subject4", Set.of(1, 2))
      ));
    }
  }

  @Test
  public void canIterateFromCursor() throws Exception {
    updatedPerPatchStore.put(0, EX + "subject1");
    updatedPerPatchStore.put(0, EX + "subject2");
    updatedPerPatchStore.put(0, EX + "subject3");
    updatedPerPatchStore.put(1, EX + "subject1");
    updatedPerPatchStore.put(1, EX + "subject3");
    updatedPerPatchStore.put(1, EX + "subject4");
    updatedPerPatchStore.put(2, EX + "subject3");
    updatedPerPatchStore.put(2, EX + "subject4");

    String cursor;
    try (Stream<SubjectCursor> subjects = updatedPerPatchStore.fromVersion(0, "")) {
      // Get the first two items and the cursor of the last one
      cursor = subjects.limit(2).reduce((first, second) -> second).orElse(null).getCursor();
    }

    try (Stream<SubjectCursor> subjects = updatedPerPatchStore.fromVersion(0, "A\n" + cursor)) {
      List<SubjectCursor> resultList = subjects.collect(toList());
      assertThat(resultList, contains(
          // Starting from the cursor and going ascending should give us the last two
          SubjectCursor.create(EX + "subject3", Set.of(0, 1, 2)),
          SubjectCursor.create(EX + "subject4", Set.of(1, 2))
      ));
    }
  }

  @Test
  public void canIterateFromSpecificVersion() throws Exception {
    updatedPerPatchStore.put(0, EX + "subject1");
    updatedPerPatchStore.put(0, EX + "subject2");
    updatedPerPatchStore.put(0, EX + "subject3");
    updatedPerPatchStore.put(1, EX + "subject1");
    updatedPerPatchStore.put(1, EX + "subject3");
    updatedPerPatchStore.put(1, EX + "subject4");
    updatedPerPatchStore.put(2, EX + "subject3");
    updatedPerPatchStore.put(2, EX + "subject4");

    try (Stream<SubjectCursor> subjects = updatedPerPatchStore.fromVersion(1, "")) {
      List<SubjectCursor> resultList = subjects.collect(toList());
      assertThat(resultList, contains(
          SubjectCursor.create(EX + "subject1", Set.of(0, 1)),
          SubjectCursor.create(EX + "subject3", Set.of(0, 1, 2)),
          SubjectCursor.create(EX + "subject4", Set.of(1, 2))
      ));
    }
  }

  @Test
  public void canIterateFromSpecificVersionAndCursor() throws Exception {
    updatedPerPatchStore.put(0, EX + "subject1");
    updatedPerPatchStore.put(0, EX + "subject2");
    updatedPerPatchStore.put(0, EX + "subject3");
    updatedPerPatchStore.put(1, EX + "subject1");
    updatedPerPatchStore.put(1, EX + "subject3");
    updatedPerPatchStore.put(1, EX + "subject4");
    updatedPerPatchStore.put(2, EX + "subject3");
    updatedPerPatchStore.put(2, EX + "subject4");

    String cursor;
    try (Stream<SubjectCursor> subjects = updatedPerPatchStore.fromVersion(1, "")) {
      // Get the first two items and the cursor of the last one
      cursor = subjects.limit(2).reduce((first, second) -> second).orElse(null).getCursor();
    }

    try (Stream<SubjectCursor> subjects = updatedPerPatchStore.fromVersion(1, "A\n" + cursor)) {
      List<SubjectCursor> resultList = subjects.collect(toList());
      assertThat(resultList, contains(
          // Starting from the cursor and going ascending should give us the last one
          SubjectCursor.create(EX + "subject4", Set.of(1, 2))
      ));
    }
  }
}
