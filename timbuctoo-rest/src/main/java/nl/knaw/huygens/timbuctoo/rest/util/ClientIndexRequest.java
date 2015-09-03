package nl.knaw.huygens.timbuctoo.rest.util;

public class ClientIndexRequest {
  private String collectionName;

  public ClientIndexRequest(){

  }

  public ClientIndexRequest(String collectionName) {
    this.collectionName = collectionName;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }
}
