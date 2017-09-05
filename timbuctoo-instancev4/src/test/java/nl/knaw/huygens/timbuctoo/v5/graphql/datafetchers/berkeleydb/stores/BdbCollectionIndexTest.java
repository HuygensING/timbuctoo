package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.stores;

import nl.knaw.huygens.timbuctoo.v5.dataset.DummyDataProvider;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbCollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.NonPersistentBdbDatabaseCreator;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;

public class BdbCollectionIndexTest {

  public static final String EX = "http://example.org/";
  protected NonPersistentBdbDatabaseCreator databaseCreator;
  protected BdbCollectionIndex collectionIndex;
  protected DummyDataProvider dataProvider;

  @Before
  public void makeCollection() throws Exception {
    databaseCreator = new NonPersistentBdbDatabaseCreator();
    dataProvider = new DummyDataProvider();
    collectionIndex = new BdbCollectionIndex(
      dataProvider,
      databaseCreator,
      "userId",
      "dataSetId"
    );
  }

  @After
  public void cleanUp() throws Exception {
    databaseCreator.close();
  }

  @Test
  public void itReturnsTheItems() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject2", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject3", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject4", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.finish();

    Stream<CursorSubject> forward = collectionIndex.getSubjects(EX + "type1", "");
    assertThat(forward.collect(Collectors.toList()), Matchers.contains(
      CursorSubject.create(EX + "subject1", EX + "subject1"),
      CursorSubject.create(EX + "subject2", EX + "subject2"),
      CursorSubject.create(EX + "subject3", EX + "subject3"),
      CursorSubject.create(EX + "subject4", EX + "subject4")
    ));
    forward.close();

  }


  @Test
  public void itPersistsAcrossRestarts() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject2", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject3", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject4", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.finish();
    collectionIndex.close();
    collectionIndex = new BdbCollectionIndex(
      dataProvider,
      databaseCreator,
      "userId",
      "dataSetId"
    );
    Stream<CursorSubject> forward = collectionIndex.getSubjects(EX + "type1", "");
    assertThat(forward.collect(Collectors.toList()), Matchers.contains(
      CursorSubject.create(EX + "subject1", EX + "subject1"),
      CursorSubject.create(EX + "subject2", EX + "subject2"),
      CursorSubject.create(EX + "subject3", EX + "subject3"),
      CursorSubject.create(EX + "subject4", EX + "subject4")
    ));
    forward.close();
  }


  @Test
  public void itHandlesForwardIterationFromACursor() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject2", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject3", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject4", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.finish();

    Stream<CursorSubject> cursorAsc = collectionIndex.getSubjects(EX + "type1", "A\n" + EX + "subject3");
    assertThat(cursorAsc.collect(Collectors.toList()), Matchers.contains(
      CursorSubject.create(EX + "subject4", EX + "subject4")
    ));
    cursorAsc.close();
  }

  @Test
  public void itHandlesBackwardIterationFromACursor() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject2", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject3", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject4", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.finish();

    Stream<CursorSubject> cursorDesc = collectionIndex.getSubjects(EX + "type1", "D\n" + EX + "subject3");
    assertThat(cursorDesc.collect(Collectors.toList()), Matchers.contains(
      CursorSubject.create(EX + "subject2", EX + "subject2"),
      CursorSubject.create(EX + "subject1", EX + "subject1")
    ));
    cursorDesc.close();
  }

  @Test
  public void itHandlesBackwardsIteration() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject2", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject3", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject4", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.finish();

    Stream<CursorSubject> last = collectionIndex.getSubjects(EX + "type1", "LAST");
    assertThat(last.collect(Collectors.toList()), Matchers.contains(
      CursorSubject.create(EX + "subject4", EX + "subject4"),
      CursorSubject.create(EX + "subject3", EX + "subject3"),
      CursorSubject.create(EX + "subject2", EX + "subject2"),
      CursorSubject.create(EX + "subject1", EX + "subject1")
    ));
    last.close();
  }

  @Test
  public void itRemovesTheSubjectOnANonAssertionRdfTypeQuad() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(true, EX + "subject1", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject2", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.onQuad(false, EX + "subject1", RDF_TYPE, EX + "type1", null, null, "http://some graph");
    dataProvider.finish();

    Stream<CursorSubject> subjects = collectionIndex.getSubjects(EX + "type1", "");
    assertThat(subjects.collect(Collectors.toList()), Matchers.contains(
      CursorSubject.create(EX + "subject2", EX + "subject2")
    ));
    subjects.close();
  }

}
