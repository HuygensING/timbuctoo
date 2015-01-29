package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
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

import static org.junit.Assert.assertEquals;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.storage.Properties;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MongoPropertyReducerTest {

  private ObjectMapper mapper;
  private Properties properties;

  @Before
  public void setup() throws Exception {
    mapper = new ObjectMapper();
    properties = new MongoProperties();
  }

  private <T> void doTest(Class<?> type, T value) throws Exception {
    JsonNode node = mapper.valueToTree(value);
    assertEquals(value, properties.reduce(type, node));
  }

  @Test
  public void testBooleanPrimitive() throws Exception {
    doTest(boolean.class, false);
    doTest(boolean.class, true);
  }

  @Test
  public void testBooleanObject() throws Exception {
    doTest(Boolean.class, Boolean.FALSE);
    doTest(Boolean.class, Boolean.TRUE);
  }

  @Test
  public void testShortPrimitive() throws Exception {
    doTest(short.class, (short) 42);
  }

  @Test
  public void testShortObject() throws Exception {
    doTest(Short.class, new Short((short) 42));
  }

  @Test
  public void testIntegerPrimitive() throws Exception {
    doTest(int.class, 42);
  }

  @Test
  public void testIntegerObject() throws Exception {
    doTest(Integer.class, new Integer(42));
  }

  @Test
  public void testLongPrimitive() throws Exception {
    doTest(long.class, 42L);
  }

  @Test
  public void testLongObject() throws Exception {
    doTest(Long.class, new Long(42L));
  }

  @Test
  public void testFloatPrimitive() throws Exception {
    doTest(float.class, 42.0F);
  }

  @Test
  public void testFloatObject() throws Exception {
    doTest(Float.class, new Float(42F));
  }

  @Test
  public void testDoublePrimitive() throws Exception {
    doTest(double.class, 42.0);
  }

  @Test
  public void testDoubleObject() throws Exception {
    doTest(Double.class, new Double(42.0));
  }

  @Test
  public void testCharacterPrimitive() throws Exception {
    doTest(char.class, 'x');
  }

  @Test
  public void testCharacterObject() throws Exception {
    doTest(Character.class, new Character('x'));
  }

  @Test
  public void testString() throws Exception {
    doTest(String.class, "xyz");
  }

  @Test
  public void testDatable() throws Exception {
    Datable datable = new Datable("19531113");
    JsonNode node = mapper.valueToTree(datable.getEDTF());
    assertEquals(datable, properties.reduce(Datable.class, node));
  }

}
