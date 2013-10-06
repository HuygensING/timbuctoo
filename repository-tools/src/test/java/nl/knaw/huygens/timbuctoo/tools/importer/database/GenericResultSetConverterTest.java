package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

import org.junit.Test;
import org.mockito.Mockito;

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
    mapping.put("test", Arrays.asList(new String[] { "test" }));

    Class<DocumentExtensionWithStringField> type = DocumentExtensionWithStringField.class;

    GenericResultSetConverter<DocumentExtensionWithStringField> instance = new GenericResultSetConverter<DocumentExtensionWithStringField>(mapping, type);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<DocumentExtensionWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue", result.get(0).getTest());
  }

  @Test
  public void testConvertWithStringOfMultipleField() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Arrays.asList(new String[] { "test1", "test2", "test3" }));

    Class<DocumentExtensionWithStringField> type = DocumentExtensionWithStringField.class;

    GenericResultSetConverter<DocumentExtensionWithStringField> instance = new GenericResultSetConverter<DocumentExtensionWithStringField>(mapping, type);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test1", "testValue1");
    resultSetMap.put("test2", "testValue2");
    resultSetMap.put("test3", "testValue3");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<DocumentExtensionWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue1 testValue2 testValue3", result.get(0).getTest());
  }

  @Test
  public void testConvertWithDatable() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("datable", Arrays.asList(new String[] { "test" }));

    Class<DocumentExtensionWithDatableField> type = DocumentExtensionWithDatableField.class;

    GenericResultSetConverter<DocumentExtensionWithDatableField> instance = new GenericResultSetConverter<DocumentExtensionWithDatableField>(mapping, type);

    String databableValue = "20130305";
    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", databableValue);

    ResultSet resultSet = createResultSet(resultSetMap);

    List<DocumentExtensionWithDatableField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());

    Datable actualDatable = result.get(0).getDatable();
    Datable expectedDatable = new Datable(databableValue);

    Assert.assertEquals(0, expectedDatable.compareTo(actualDatable));
  }

  @Test
  public void testConvertWithNoneMappedFields() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();

    Class<DocumentExtensionWithStringField> type = DocumentExtensionWithStringField.class;

    GenericResultSetConverter<DocumentExtensionWithStringField> instance = new GenericResultSetConverter<DocumentExtensionWithStringField>(mapping, type);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<DocumentExtensionWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void testConvertWithExtendedMappings() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Arrays.asList(new String[] { "test" }));

    Class<DocumentExtensionWithStringField> type = DocumentExtensionWithStringField.class;

    GenericResultSetConverter<DocumentExtensionWithStringField> instance = new GenericResultSetConverter<DocumentExtensionWithStringField>(mapping, type);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");
    resultSetMap.put("test1", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<DocumentExtensionWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue", result.get(0).getTest());
  }

  @Test()
  public void testConvertWithFieldsNotInResultSet() throws InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Arrays.asList(new String[] { "test", "test2" }));

    Class<DocumentExtensionWithStringField> type = DocumentExtensionWithStringField.class;

    GenericResultSetConverter<DocumentExtensionWithStringField> instance = new GenericResultSetConverter<DocumentExtensionWithStringField>(mapping, type);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<DocumentExtensionWithStringField> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue", result.get(0).getTest());
  }

  @Test
  public void testConvertSubclass() throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
    Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    mapping.put("test", Arrays.asList(new String[] { "test" }));

    Class<SubDocumentExtension> type = SubDocumentExtension.class;

    GenericResultSetConverter<SubDocumentExtension> instance = new GenericResultSetConverter<SubDocumentExtension>(mapping, type);

    Map<String, String> resultSetMap = new HashMap<String, String>();
    resultSetMap.put("test", "testValue");

    ResultSet resultSet = createResultSet(resultSetMap);

    List<SubDocumentExtension> result = instance.convert(resultSet);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals("testValue", result.get(0).getTest());
  }
}
