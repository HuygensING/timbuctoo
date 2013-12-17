package nl.knaw.huygens.timbuctoo.tools.importer;

/*
 * #%L
 * Timbuctoo tools
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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.tools.importer.GenericResultSetConverter;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

public class GenericResultSetConverterTest {

  private ResultSet createResultSet(Map<String, String> fields) throws SQLException {
    ResultSet resultSet = Mockito.mock(ResultSet.class);
    Mockito.when(resultSet.next()).thenReturn(true).thenReturn(false);

    Set<String> keys = fields.keySet();

    for (String key : keys) {
      Mockito.when(resultSet.getString(key)).thenReturn(fields.get(key));
    }

    return resultSet;
  }

  @Test
  public void testConvertWithStringOfSingleField() throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Lists.newArrayList("test"));

    Class<EntityWithStringField> type = EntityWithStringField.class;

    GenericResultSetConverter<EntityWithStringField> instance = new GenericResultSetConverter<EntityWithStringField>(type, mapping, null);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<EntityWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue", result.get(0).getTest());
  }

  @Test
  public void testConvertWithStringOfMultipleField() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Lists.newArrayList("test1", "test2", "test3"));

    Class<EntityWithStringField> type = EntityWithStringField.class;

    GenericResultSetConverter<EntityWithStringField> instance = new GenericResultSetConverter<EntityWithStringField>(type, mapping, null);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test1", "testValue1");
    resultSetMap.put("test2", "testValue2");
    resultSetMap.put("test3", "testValue3");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<EntityWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue1 testValue2 testValue3", result.get(0).getTest());
  }

  @Test
  public void testConvertWithDatable() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("datable", Lists.newArrayList("test"));

    Class<EntityWithDatableField> type = EntityWithDatableField.class;

    GenericResultSetConverter<EntityWithDatableField> instance = new GenericResultSetConverter<EntityWithDatableField>(type, mapping, null);

    String databableValue = "20130305";
    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", databableValue);

    ResultSet resultSet = createResultSet(resultSetMap);

    List<EntityWithDatableField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());

    Datable actualDatable = result.get(0).getDatable();
    Datable expectedDatable = new Datable(databableValue);

    Assert.assertEquals(0, expectedDatable.compareTo(actualDatable));
  }

  @Test
  public void testConvertWithNoneMappedFields() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();

    Class<EntityWithStringField> type = EntityWithStringField.class;

    GenericResultSetConverter<EntityWithStringField> instance = new GenericResultSetConverter<EntityWithStringField>(type, mapping, null);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<EntityWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void testConvertWithExtendedMappings() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Lists.newArrayList("test"));

    Class<EntityWithStringField> type = EntityWithStringField.class;

    GenericResultSetConverter<EntityWithStringField> instance = new GenericResultSetConverter<EntityWithStringField>(type, mapping, null);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");
    resultSetMap.put("test1", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<EntityWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue", result.get(0).getTest());
  }

  @Test()
  public void testConvertWithFieldsNotInResultSet() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Lists.newArrayList("test", "test2"));

    Class<EntityWithStringField> type = EntityWithStringField.class;

    GenericResultSetConverter<EntityWithStringField> instance = new GenericResultSetConverter<EntityWithStringField>(type, mapping, null);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<EntityWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue", result.get(0).getTest());
  }

  @Test
  public void testConvertSubclass() throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Lists.newArrayList("test"));

    Class<SubEntityExtension> type = SubEntityExtension.class;

    GenericResultSetConverter<SubEntityExtension> instance = new GenericResultSetConverter<SubEntityExtension>(type, mapping, null);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<SubEntityExtension> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue", result.get(0).getTest());
  }

  @Test
  public void testConvertClassWithRoles() throws SQLException, SecurityException, IllegalArgumentException, InstantiationException, IllegalAccessException, NoSuchMethodException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Lists.newArrayList("test"));
    mapping.put("ImportTestRole.roleTest", Lists.newArrayList("roleTest"));

    List<EntityWithStringField> expected = Lists.newArrayList();

    EntityWithStringField entityWithStringField = new EntityWithStringField();
    entityWithStringField.setTest("testValue");
    ImportTestRole testRole = new ImportTestRole();
    testRole.setRoleTest("anotherTestValue");
    entityWithStringField.addRole(testRole);
    expected.add(entityWithStringField);

    Class<EntityWithStringField> type = EntityWithStringField.class;
    List<Class<? extends Role>> allowedRoles = Lists.newArrayList();
    allowedRoles.add(ImportTestRole.class);

    GenericResultSetConverter<EntityWithStringField> instance = new GenericResultSetConverter<EntityWithStringField>(type, mapping, allowedRoles);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    String testValue = "testValue";
    resultSetMap.put("test", testValue);
    resultSetMap.put("roleTest", "anotherTestValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<EntityWithStringField> actual = instance.convert(resultSet);

    assertEquals(expected, actual);
  }
}
