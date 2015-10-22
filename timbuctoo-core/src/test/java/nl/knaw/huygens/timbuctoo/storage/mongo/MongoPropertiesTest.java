package nl.knaw.huygens.timbuctoo.storage.mongo;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.storage.Properties;
import org.junit.Before;
import org.junit.Test;
import test.model.TestSystemEntityWrapper;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MongoPropertiesTest {

  public static final Class<TestSystemEntityWrapper> TYPE_WITH_FIELDS_TO_REDUCE = TestSystemEntityWrapper.class;
  private ObjectMapper mapper;
  private Properties properties;

  @Before
  public void setup() throws Exception {
    mapper = new ObjectMapper();
    properties = new MongoProperties();
  }

  private <T> void doTest(String objectCollection, T value) throws Exception {
    Field field = getField(objectCollection);
    JsonNode node = mapper.valueToTree(value);

    assertEquals(value, properties.reduce(field, node));
  }

  private Field getField(String testBooleanObject) throws NoSuchFieldException {
    return TYPE_WITH_FIELDS_TO_REDUCE.getDeclaredField(testBooleanObject);
  }

  @Test
  public void testBooleanPrimitive() throws Exception {
    doTest("booleanPrimitive", false);
    doTest("booleanPrimitive", true);
  }

  @Test
  public void testBooleanObject() throws Exception {
    doTest("booleanObject", Boolean.FALSE);
    doTest("booleanObject", Boolean.TRUE);
  }



  @Test
  public void testShortPrimitive() throws Exception {
    doTest("shortValue", (short) 42);
  }

  @Test
  public void testShortObject() throws Exception {
    doTest("shortObject", new Short((short) 42));
  }

  @Test
  public void testIntegerPrimitive() throws Exception {
    doTest("intPrimitive", 42);
  }

  @Test
  public void testIntegerObject() throws Exception {
    doTest("intObject", new Integer(42));
  }

  @Test
  public void testLongPrimitive() throws Exception {
    doTest("longPrimitive", 42L);
  }

  @Test
  public void testLongObject() throws Exception {
    doTest("longObject", new Long(42L));
  }

  @Test
  public void testFloatPrimitive() throws Exception {
    doTest("floatPrimitive", 42.0F);
  }

  @Test
  public void testFloatObject() throws Exception {
    doTest("floatObject", new Float(42F));
  }

  @Test
  public void testDoublePrimitive() throws Exception {
    doTest("doublePrimitive", 42.0);
  }

  @Test
  public void testDoubleObject() throws Exception {
    doTest("doubleObject", new Double(42.0));
  }

  @Test
  public void testCharacterPrimitive() throws Exception {
    doTest("charPrimitive", 'x');
  }

  @Test
  public void testCharacterObject() throws Exception {
    doTest("charObject", new Character('x'));
  }

  @Test
  public void testString() throws Exception {
    doTest("stringObject", "xyz");
  }

  @Test
  public void testDatable() throws Exception {
    Datable datable = new Datable("19531113");
    JsonNode node = mapper.valueToTree(datable.getEDTF());
    assertEquals(datable, properties.reduce(getField("datableObject"), node));
  }

  @Test
  public void reduceDoesNotReduceAListWithObjectsToAListWithLinkedHashMaps() throws Exception {
    // setup
    Change change1 = Change.newInternalInstance();
    Change change2 = Change.newInternalInstance();

    List<Change> value = Lists.newArrayList(change1, change2);

    // action
    doTest("objectCollection", value);
  }

}
