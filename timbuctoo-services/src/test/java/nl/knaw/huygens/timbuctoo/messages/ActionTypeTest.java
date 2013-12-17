package nl.knaw.huygens.timbuctoo.messages;

/*
 * #%L
 * Timbuctoo services
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
