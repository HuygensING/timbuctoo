package nl.knaw.huygens.timbuctoo.messages;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class Action {

  private final ActionType actionType;
  private final String id;
  private final Class<? extends DomainEntity> type;

  public Action(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    this.actionType = actionType;
    this.id = id;
    this.type = type;
  }

  public ActionType getActionType() {
    return actionType;
  }

  public String getId() {
    return id;
  }

  public Class<? extends DomainEntity> getType() {
    return type;
  }

  @Override
  public String toString() {
    return "actionType: " + actionType + "\ntypeString: " + type + "\nid: " + id;
  }

}
