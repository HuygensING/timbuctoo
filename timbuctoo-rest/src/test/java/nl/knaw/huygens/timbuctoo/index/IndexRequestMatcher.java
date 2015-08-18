package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class IndexRequestMatcher extends CompositeMatcher<IndexRequest> {
  private IndexRequestMatcher() {

  }

  public static IndexRequestMatcher likeIndexRequest() {
    return new IndexRequestMatcher();
  }

  public IndexRequestMatcher forType(Class<? extends DomainEntity> type) {
    this.addMatcher(new PropertyEqualtityMatcher<IndexRequest, Class<? extends DomainEntity>>("type", type) {
      @Override
      protected Class<? extends DomainEntity> getItemValue(IndexRequest item) {
        return item.getType();
      }
    });
    return this;
  }

  public IndexRequestMatcher forId(String id) {
    this.addMatcher(new PropertyEqualtityMatcher<IndexRequest, String>("type", id) {
      @Override
      protected String getItemValue(IndexRequest item) {
        return item.getId();
      }
    });
    return this;
  }
}
