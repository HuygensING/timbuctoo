package nl.knaw.huygens.timbuctoo.messages;

/**
 * Changed the String representation to an enum.
 * It's designed after http://stackoverflow.com/a/2965252
 * 
 * @author martijnm
 */
public enum ActionType {
  ADD("add"), DEL("del"), END("end"), MOD("mod");

  private final String stringRepresentation;

  ActionType(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  public String getStringRepresentation() {
    return stringRepresentation;
  }

  public static ActionType getFromString(String stringRepresentation) {
    if (stringRepresentation != null) {
      for (ActionType actionType : values()) {
        if (actionType.getStringRepresentation().equals(stringRepresentation)) {
          return actionType;
        }
      }
    }
    return null;
  }

}
