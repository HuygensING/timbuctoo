package nl.knaw.huygens.timbuctoo.util;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class TextTest {

  private static final String WHITE = "\t\n \u00A0";

  @Test
  public void testNormalizeNull() {
    assertThat(Text.normalizeWhitespace(null), equalTo(""));
  }

  @Test
  public void testNormalizeNoWhitespace() {
    assertThat(Text.normalizeWhitespace("abc"), equalTo("abc"));
  }

  @Test
  public void testNormalizeLeadingWhitespace() {
    assertThat(Text.normalizeWhitespace(WHITE + "abc"), equalTo("abc"));
  }

  @Test
  public void testNormalizeTrailingWhitespace() {
    assertThat(Text.normalizeWhitespace("abc" + WHITE), equalTo("abc"));
  }

  @Test
  public void testNormalizeEmbeddedWhitespace() {
    assertThat(Text.normalizeWhitespace("abc" + WHITE + "def"), equalTo("abc def"));
  }

}
