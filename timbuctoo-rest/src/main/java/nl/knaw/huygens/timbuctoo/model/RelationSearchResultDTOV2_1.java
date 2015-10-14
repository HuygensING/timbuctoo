package nl.knaw.huygens.timbuctoo.model;

public class RelationSearchResultDTOV2_1 extends RegularSearchResultDTO{
  private String otherSearchId;

  private String getOtherSearchId() {
    return otherSearchId;
  }

  private void setOtherSearchId(String otherSearchId) {
    this.otherSearchId = otherSearchId;
  }
}
