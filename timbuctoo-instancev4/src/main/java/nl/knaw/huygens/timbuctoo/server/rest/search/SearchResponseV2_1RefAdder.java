package nl.knaw.huygens.timbuctoo.server.rest.search;

import nl.knaw.huygens.timbuctoo.server.search.EntityRef;

public class SearchResponseV2_1RefAdder {
  public void addRef(SearchResponseV2_1 searchResponse, EntityRef entityRef) {
    SearchResponseV2_1Ref searchResponseRef = new SearchResponseV2_1Ref(
      entityRef.getId(),
      entityRef.getType(),
      createPath(entityRef),
      entityRef.getDisplayName(),
      entityRef.getData());


    searchResponse.addRef(searchResponseRef);
  }

  private String createPath(EntityRef entityRef) {
    return String.format("v2.1/domain/%ss/%s", entityRef.getType(), entityRef.getId());
  }
}
