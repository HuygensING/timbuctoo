package nl.knaw.huygens.timbuctoo;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class DomainEntityMatcher extends CompositeMatcher<DomainEntity>{

  private DomainEntityMatcher(){

  }

  public static DomainEntityMatcher likeDomainEntity(){
    return new DomainEntityMatcher();
  }

  public DomainEntityMatcher ofType(Class<? extends DomainEntity> type){
    this.addMatcher(new PropertyEqualityMatcher<DomainEntity, Class<? extends DomainEntity>>("type", type) {
      @Override
      protected Class<? extends DomainEntity> getItemValue(DomainEntity item) {
        return item.getClass();
      }
    });

    return this;
  }

  public DomainEntityMatcher withId(String id){
    this.addMatcher(new PropertyEqualityMatcher<DomainEntity, String>("id", id) {
      @Override
      protected String getItemValue(DomainEntity item) {
        return item.getId();
      }
    });

    return this;
  }

  public DomainEntityMatcher withRevision(int revision){
    this.addMatcher(new PropertyEqualityMatcher<DomainEntity, Integer>("revision", revision) {
      @Override
      protected Integer getItemValue(DomainEntity item) {
        return item.getRev();
      }
    });

    return this;
  }

  public DomainEntityMatcher withPID(String pid){
    this.addMatcher(new PropertyEqualityMatcher<DomainEntity, String>("pid", pid) {
      @Override
      protected String getItemValue(DomainEntity item) {
        return item.getPid();
      }
    });

    return this;
  }


}
