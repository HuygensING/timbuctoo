package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.stores;

import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.DummyDataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.NonPersistentBdbDatabaseCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad.create;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.LANGSTRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

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

    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "")) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject1", null, null, ""),
        create(EX + "subject1", "http://pred", Direction.OUT, "12", "http://number", null, ""),
        create(EX + "subject1", "http://pred", Direction.OUT, "Walter", LANGSTRING, "EN-en", ""),
        create(EX + "subject1", "http://pred", Direction.OUT, "Gauthier", LANGSTRING, "FR-fr", "")
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

    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "")) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject1", null, null, ""),
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject2", null, null, ""),
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject3", null, null, ""),
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject4", null, null, "")
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

    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "LAST")) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject4", null, null, ""),
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject3", null, null, ""),
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject2", null, null, ""),
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject1", null, null, "")
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
    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "")) {
      //get the first two items and the cursor of the last one
      cursor = quads.limit(2).reduce((first, second) -> second).orElse(null).getCursor();
    }
    try (Stream<CursorQuad> quads =
           tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "A\n" + cursor)) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        //starting from the cursor and going ascending should give us the last two
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject3", null, null, ""),
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject4", null, null, "")
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
    try (Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "")) {
      //get the first two items and the cursor of the last one
      cursor = quads.limit(2).reduce((first, second) -> second).orElse(null).getCursor();
    }
    try (Stream<CursorQuad> quads =
           tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "D\n" + cursor)) {
      List<CursorQuad> resultList = quads.collect(toList());
      assertThat(resultList, contains(
        //starting from the cursor and going descending should give us the first
        create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject1", null, null, "")
      ));
    }
  }

  @Test
  public void relationQuadRetractionRemovesTheQuadFromTheStore() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(true, EX + "subject1", "http://pred", EX + "subject1", null, null, "http://some graph");
    dataProvider.onQuad(true, EX + "subject1", "http://pred", EX + "subject2", null, null, "http://some graph");
    dataProvider.onQuad(false, EX + "subject1", "http://pred", EX + "subject2", null, null, "http://some graph");
    dataProvider.finish();

    Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "");
    assertThat(quads.collect(toList()), not(hasItem(
      create(EX + "subject1", "http://pred", Direction.OUT, EX + "subject2", null, null, "http://some graph")
    )));
    quads.close();
  }



  @Test
  public void relationQuadRetractionRemovesTheReverseRelationQuadFromTheStore() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(true, EX + "subject1", "http://pred", EX + "subject1", null, null, "http://some graph");
    dataProvider.onQuad(true, EX + "subject1", "http://pred", EX + "subject2", null, null, "http://some graph");
    dataProvider.onQuad(false, EX + "subject1", "http://pred", EX + "subject2", null, null, "http://some graph");
    dataProvider.finish();

    Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject2", "http://pred_inverse", Direction.OUT, "");
    assertThat(quads.collect(toList()), not(hasItem(
      create(EX + "subject2", "http://pred_inverse", Direction.OUT, EX + "subject1", null, null, "http://some graph")
    )));
    quads.close();
  }

  @Test
  public void langStringQuadRetractionQuadRemovesTheQuadFromTheStore() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(true, EX + "subject1", "http://pred", "Walter", LANGSTRING, "EN-en", "http://some graph");
    dataProvider.onQuad(true, EX + "subject1", "http://pred", "Gauthier", LANGSTRING, "FR-fr", "http://some graph");
    dataProvider.onQuad(false, EX + "subject1", "http://pred", "Walter", LANGSTRING, "EN-en", "http://some graph");
    dataProvider.finish();

    Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "");
    assertThat(quads.collect(toList()), not(hasItem(
      create(EX + "subject1", "http://pred", Direction.OUT, "Walter", LANGSTRING, "EN-en", "http://some graph")
    )));
    quads.close();
  }

  @Test
  public void valueQuadRetractionQuadRemovesTheQuadFromTheStore() throws Exception {
    dataProvider.start();
    dataProvider.onQuad(true, EX + "subject1", "http://pred", "12", "http://number", null, "http://some graph");
    dataProvider.onQuad(true, EX + "subject1", "http://pred", "14", "http://number", null, "http://some graph");
    dataProvider.onQuad(false, EX + "subject1", "http://pred", "12", "http://number", null, "http://some graph");
    dataProvider.finish();

    Stream<CursorQuad> quads = tripleStore.getQuads(EX + "subject1", "http://pred", Direction.OUT, "");
    assertThat(quads.collect(toList()), not(hasItem(
      create(EX + "subject1", "http://pred", Direction.OUT, "Walter", LANGSTRING, "EN-en", "http://some graph")
    )));
    quads.close();
  }

}
