package nl.knaw.huygens.timbuctoo.model;

public class MongoObjectMapperNested extends SystemEntity {

  private MongoObjectMapperEntity nestedEntity;

  public MongoObjectMapperNested(MongoObjectMapperEntity nestedEntity) {
    this.nestedEntity = nestedEntity;
  }

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  public MongoObjectMapperEntity getNestedEntity() {
    return nestedEntity;
  }

  public void setNestedEntity(MongoObjectMapperEntity nestedEntity) {
    this.nestedEntity = nestedEntity;
  }

}
