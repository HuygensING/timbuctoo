package nl.knaw.huygens.repository.messages;

/**
 * Changed the String representation to an enum.
 * It's designed after http://stackoverflow.com/a/2965252
 * 
 * @author martijnm
 *
 */
public enum ActionType {
  INDEX_ADD("add"), INDEX_DEL("del"), INDEX_END("end"), INDEX_MOD("mod");

  private String stringRepresentation;

  ActionType(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  public String getStringRepresentation() {
    return stringRepresentation;
  }

  public static ActionType getFromString(String stringRepresentation) {
    if (stringRepresentation == null) {
      return null;
    }

    for (ActionType actionType : values()) {
      if (actionType.getStringRepresentation().equals(stringRepresentation)) {
        return actionType;
      }
    }

    return null;
  }
}
