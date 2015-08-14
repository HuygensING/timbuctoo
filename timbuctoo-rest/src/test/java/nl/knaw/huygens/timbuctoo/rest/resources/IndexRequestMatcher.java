package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.timbuctoo.index.IndexRequest;

public class IndexRequestMatcher extends CompositeMatcher<IndexRequest> {
  private IndexRequestMatcher() {

  }

  public static IndexRequestMatcher likeIndexRequestMatcher() {
    return new IndexRequestMatcher();
  }

  public IndexRequestMatcher withDesc(String desc) {
    this.addMatcher(new PropertyEqualtityMatcher<IndexRequest, String>("desc", desc) {

      @Override
      protected String getItemValue(IndexRequest item) {
        return item.getDesc();
      }
    });
    return this;
  }
}
