package nl.knaw.huygens.timbuctoo.messages;

public class Action {

  private ActionType actionType;
  private String id;
  private String typeString;

  public Action(ActionType actionType, String typeString, String id) {
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

  @Override
  public String toString() {
    return "actionType: " + actionType + "\ntypeString: " + typeString + "\nid: " + id;
  }

}
