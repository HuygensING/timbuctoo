package nl.knaw.huygens.timbuctoo.messages;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class ActionMatcher extends CompositeMatcher<Action> {

  private ActionMatcher() {

  }

  public static ActionMatcher likeAction() {
    return new ActionMatcher();
  }

  public ActionMatcher withActionType(ActionType actionType) {
    this.addMatcher(new PropertyEqualtityMatcher<Action, ActionType>("actionType", actionType) {
      @Override
      protected ActionType getItemValue(Action item) {
        return item.getActionType();
      }
    });
    return this;
  }

  public ActionMatcher withType(Class<? extends DomainEntity> type) {
    this.addMatcher(new PropertyEqualtityMatcher<Action, Class<? extends DomainEntity>>("type", type) {
      @Override
      protected Class<? extends DomainEntity> getItemValue(Action item) {
        return item.getType();
      }
    });
    return this;
  }

  public ActionMatcher withId(String id) {
    this.addMatcher(new PropertyEqualtityMatcher<Action, String>("id", id) {
      @Override
      protected String getItemValue(Action item) {
        return item.getId();
      }
    });
    return this;
  }

  public ActionMatcher withForMultiEntitiesFlag(boolean forMultiEntitiesFlag){
    this.addMatcher(new PropertyEqualtityMatcher<Action, Boolean>("forMultiEntities", forMultiEntitiesFlag) {
      @Override
      protected Boolean getItemValue(Action item) {
        return item.isForMultiEntities();
      }
    });
    return this;
  }
}
