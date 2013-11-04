package nl.knaw.huygens.timbuctoo.messages;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.Test;

public class ActionTypeTest {

  @Test
  public void testGetFromString() {
    assertNull(ActionType.getFromString(null));
    assertNull(ActionType.getFromString("undefined"));
    assertEquals(ActionType.ADD, ActionType.getFromString("add"));
    assertEquals(ActionType.DEL, ActionType.getFromString("del"));
    assertEquals(ActionType.END, ActionType.getFromString("end"));
    assertEquals(ActionType.MOD, ActionType.getFromString("mod"));
  }

}
