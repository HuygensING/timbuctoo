package nl.knaw.huygens.repository.messages;

public class Action {
  private ActionType actionType;
  private String id;
  private String typeString;

  public Action(ActionType actionType, String id, String typeString) {
    this.actionType = actionType;
    this.id = id;
    this.typeString = typeString;
  }

  public ActionType getActionType() {
    return actionType;
  }

  public String getId() {
    return id;
  }

  public String getTypeString() {
    return typeString;
  }

}
