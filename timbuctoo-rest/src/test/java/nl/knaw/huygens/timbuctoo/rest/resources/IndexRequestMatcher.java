package nl.knaw.huygens.timbuctoo.rest.resources;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.timbuctoo.index.IndexRequest;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class IndexRequestMatcher extends CompositeMatcher<IndexRequest> {
  private IndexRequestMatcher() {

  }

  public static IndexRequestMatcher likeIndexRequest() {
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

public IndexRequestMatcher withType(Class<? extends DomainEntity> type) {
  this.addMatcher(new PropertyEqualtityMatcher<IndexRequest, Class<? extends DomainEntity>>( "type", type) {
    @Override
    protected Class<? extends DomainEntity> getItemValue(IndexRequest item) {
      return item.getType();
    }
  });
  return this;
  }
}
