package nl.knaw.huygens.timbuctoo.tools.util;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import org.junit.Assert;
import org.junit.Test;

public class EncodingFixerTest {

  @Test
  public void testConversion() {
    Assert.assertEquals("Belvédère", EncodingFixer.convert1("BelvÃ©dÃ¨re"));
    Assert.assertEquals("Curaçao", EncodingFixer.convert1("CuraÃ§ao"));
    Assert.assertEquals("Wolffenbüttel", EncodingFixer.convert1("WolffenbÃ¼ttel"));
    Assert.assertEquals("notariële", EncodingFixer.convert1("notariÃ«le"));
  }

  @Test
  public void testInvariance() {
    Assert.assertEquals("Belvédère", EncodingFixer.convert1("Belvédère"));
    Assert.assertEquals("Curaçao", EncodingFixer.convert1("Curaçao"));
  }

}
