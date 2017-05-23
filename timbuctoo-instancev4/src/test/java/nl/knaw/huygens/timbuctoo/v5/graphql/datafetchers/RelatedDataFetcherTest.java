package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb.BdbTripleStoreMaker;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.dto.Quad;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginatedList;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb.BdbCollectionStoreMaker.collectionIndex;

public class RelatedDataFetcherTest {

  private String ex(String postfix) {
    return "http://example.org/" + postfix;
  }

  @Test
  public void returnsPredicates() throws Exception {
    List<Quad> quads = Lists.newArrayList(
      Quad.create(ex("subj"), ex("pred"), ex("obj"), null, null, ex("graph")),
      Quad.create(ex("subj"), ex("pred"), ex("obj2"), null, null, ex("graph")),
      Quad.create(ex("subj"), ex("pred"), ex("obj3"), null, null, ex("graph")),
      Quad.create(ex("subj"), ex("pred"), ex("obj4"), null, null, ex("graph")),
      Quad.create(ex("subj"), ex("pred"), ex("obj5"), null, null, ex("graph")),
      Quad.create(ex("subj"), ex("pred"), ex("obj6"), null, null, ex("graph"))
    );
    try (BdbTripleStoreMaker.CloseableTempStore tempStore = BdbTripleStoreMaker.make(quads)) {
      RelatedDataFetcher relatedDataFetcher = new RelatedDataFetcher(ex("pred"), tempStore.getStore(), true) {
        @Override
        protected BoundSubject makeItem(Quad quad) {
          return new BoundSubject(quad.getSubject() + " - " + quad.getObject());
        }
      };
      PaginatedList result = relatedDataFetcher.getList(
        new BoundSubject(ex("subj")),
        null,
        null,
        3,
        "http://example.org/subj\nhttp://example.org/pred\n\n\nhttp://example.org/obj5"
        );
      result.getPageInfo().getEndCursor();
    }
  }


  @Test
  public void returnsPredicates2() throws Exception {
    ListMultimap<String, String> multimap = MultimapBuilder.hashKeys().arrayListValues().build();
    multimap.put(ex("collection"), ex("subj1"));
    multimap.put(ex("collection"), ex("subj2"));
    multimap.put(ex("collection"), ex("subj3"));
    multimap.put(ex("collection"), ex("subj4"));
    multimap.put(ex("collection"), ex("subj5"));
    multimap.put(ex("collection"), ex("subj6"));
    CollectionDataFetcher fetcher = new CollectionDataFetcher(ex("collection"), collectionIndex(multimap));
    PaginatedList result = fetcher.getList(
      "http://example.org/subj3",
      null,
      2,
      null
    );
    result.getPageInfo().getEndCursor();
  }

  //fixme: if I look for an unknown subject I still get one result
  //fixme: max 20
  //FIXME: edge cases (last item, first item exactly the amount of items in the list)

}
