package nl.knaw.huygens.timbuctoo;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class DomainEntityMatcher extends CompositeMatcher<DomainEntity>{

  private DomainEntityMatcher(){

  }

  public static DomainEntityMatcher likeDomainEntity(){
    return new DomainEntityMatcher();
  }

  public DomainEntityMatcher ofType(Class<? extends DomainEntity> type){
    this.addMatcher(new PropertyEqualtityMatcher<DomainEntity, Class<? extends DomainEntity>>("type", type) {
      @Override
      protected Class<? extends DomainEntity> getItemValue(DomainEntity item) {
        return item.getClass();
      }
    });

    return this;
  }

  public DomainEntityMatcher withId(String id){
    this.addMatcher(new PropertyEqualtityMatcher<DomainEntity, String>("id", id) {
      @Override
      protected String getItemValue(DomainEntity item) {
        return item.getId();
      }
    });

    return this;
  }

  public DomainEntityMatcher withRevision(int revision){
    this.addMatcher(new PropertyEqualtityMatcher<DomainEntity, Integer>("revision", revision) {
      @Override
      protected Integer getItemValue(DomainEntity item) {
        return item.getRev();
      }
    });

    return this;
  }

  public DomainEntityMatcher withPID(String pid){
    this.addMatcher(new PropertyEqualtityMatcher<DomainEntity, String>("pid", pid) {
      @Override
      protected String getItemValue(DomainEntity item) {
        return item.getPid();
      }
    });

    return this;
  }


}
