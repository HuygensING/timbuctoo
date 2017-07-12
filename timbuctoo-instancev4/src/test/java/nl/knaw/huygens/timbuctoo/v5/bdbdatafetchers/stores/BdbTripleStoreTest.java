package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.DummyDataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.NonPersistentBdbDatabaseCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.LANGSTRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class BdbTripleStoreTest {


  public static final String EX = "http://example.org/";
  protected NonPersistentBdbDatabaseCreator databaseCreator;
  protected BdbTripleStore tripleStore;
  protected DummyDataProvider dataProvider;

  @Before
  public void makeCollection() throws Exception {
    databaseCreator = new NonPersistentBdbDatabaseCreator();
    dataProvider = new DummyDataProvider();
    tripleStore = new BdbTripleStore(
      dataProvider,
      databaseCreator,
      "userId",
      "dataSetId"
    );
    Thread.sleep(2000); // to make the test work on slow systems
  }

  @After
  public void cleanUp() throws Exception {
    databaseCreator.close();
  }

  @Test
  public void returnsTheData() throws RdfProcessingFailedException {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", "12", "http://number", null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", "Walter", LANGSTRING, "EN-en", "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", "Gauthier", LANGSTRING, "FR-fr", "http://some graph");
    dataProvider.finish();

    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", "")) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject1", null, null, ""),
        CursorQuad.create(EX + "subject1", "http://pred", "12", "http://number", null, ""),
        CursorQuad.create(EX + "subject1", "http://pred", "Walter", LANGSTRING, "EN-en", ""),
        CursorQuad.create(EX + "subject1", "http://pred", "Gauthier", LANGSTRING, "FR-fr", "")
      ));
    }
  }

  @Test
  public void canIterateForward() throws RdfProcessingFailedException {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject2", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject3", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject4", null, null, "http://some graph");
    dataProvider.finish();

    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", "")) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject1", null, null, ""),
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject2", null, null, ""),
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject3", null, null, ""),
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject4", null, null, "")
      ));
    }
  }

  @Test
  public void canIterateBackward() throws RdfProcessingFailedException {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject2", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject3", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject4", null, null, "http://some graph");
    dataProvider.finish();

    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", "LAST")) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject4", null, null, ""),
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject3", null, null, ""),
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject2", null, null, ""),
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject1", null, null, "")
      ));
    }
  }

  @Test
  public void canIterateForwardFromCursor() throws RdfProcessingFailedException {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject2", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject3", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject4", null, null, "http://some graph");
    dataProvider.finish();

    String cursor;
    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", "")) {
      //get the first two items and the cursor of the last one
      cursor = quads.limit(2).reduce((first, second) -> second).orElse(null).getCursor();
    }
    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", "A\n" + cursor)) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        //starting from the cursor and going ascending should give us the last two
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject3", null, null, ""),
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject4", null, null, "")
      ));
    }
  }


  @Test
  public void canIterateBackwardFromCursor() throws RdfProcessingFailedException {
    dataProvider.start();
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject1", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject2", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject3", null, null, "http://some graph");
    dataProvider.onQuad(EX + "subject1", "http://pred", EX + "subject4", null, null, "http://some graph");
    dataProvider.finish();

    String cursor;
    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", "")) {
      //get the first two items and the cursor of the last one
      cursor = quads.limit(2).reduce((first, second) -> second).orElse(null).getCursor();
    }
    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", "D\n" + cursor)) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        //starting from the cursor and going descending should give us the first
        CursorQuad.create(EX + "subject1", "http://pred", EX + "subject1", null, null, "")
      ));
    }
  }

}
