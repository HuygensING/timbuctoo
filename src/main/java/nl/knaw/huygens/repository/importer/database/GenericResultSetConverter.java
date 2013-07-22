package nl.knaw.huygens.repository.importer.database;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Datable;
import nl.knaw.huygens.repository.model.util.PersonName;
import nl.knaw.huygens.repository.model.util.PersonNameComponent.Type;

import org.apache.commons.lang.StringUtils;

public class GenericResultSetConverter<T extends Document> {
  private Map<String, List<String>> propertyMapping;
  private Class<T> type;

  public GenericResultSetConverter(Map<String, List<String>> propertyMapping, Class<T> type) {
    this.propertyMapping = propertyMapping;
    this.type = type;
  }

  public List<T> convert(ResultSet resultSet) throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    List<T> returnValue = new ArrayList<T>();

    //Field[] fields = type.getDeclaredFields();

    List<Field> fields = new ArrayList<Field>();

    getAllFields(fields, type);

    T instance = null;

    while (resultSet.next()) {

      instance = type.newInstance();

      for (Field field : fields) {
        if (propertyMapping.containsKey(field.getName())) {
          field.setAccessible(true);

          Object fieldValue = getFieldValue(resultSet, propertyMapping.get(field.getName()), field.getType());

          field.set(instance, fieldValue);
        }
      }

      returnValue.add(instance);
    }

    return returnValue;
  }

  private <U> void getAllFields(List<Field> fieldList, Class<U> type) {
    fieldList.addAll(Arrays.asList(type.getDeclaredFields()));
    if (type.getSuperclass() != null) {
      getAllFields(fieldList, type.getSuperclass());
    }
  }

  private <U> Object getFieldValue(ResultSet resultSet, List<String> fields, Class<U> type) throws SQLException {
    if (type == String.class) {
      return getStringValue(resultSet, fields);
    } else if (type == Datable.class) {
      return new Datable(resultSet.getString(fields.get(0)));
    } else if (type == Boolean.class || type == boolean.class) {
      return resultSet.getBoolean(fields.get(0));
    } else if (type == PersonName.class) {
      return getPersonNameValue(resultSet, fields);
    } else {
      throw new RuntimeException(type.getName() + " not supported yet.");
    }
  }

  private String getStringValue(ResultSet resultSet, String field) throws SQLException {
    return (field.length() > 1) ? StringUtils.trimToEmpty(resultSet.getString(field)) : "";
  }

  private String getStringValue(ResultSet resultSet, List<String> fields) throws SQLException {
    StringBuilder builder = new StringBuilder();
    for (String field : fields) {
      String value = getStringValue(resultSet, field);
      if (value.length() != 0) {
        if (builder.length() != 0) {
          builder.append(' ');
        }
        builder.append(value);
      }
    }
    return builder.toString();
  }

  private PersonName getPersonNameValue(ResultSet resultSet, List<String> fields) throws SQLException {
    PersonName name = new PersonName();
    name.addNameComponent(Type.ROLE_NAME, getStringValue(resultSet, fields.get(0)));
    name.addNameComponent(Type.FORENAME, getStringValue(resultSet, fields.get(1)));
    name.addNameComponent(Type.NAME_LINK, getStringValue(resultSet, fields.get(2)));
    name.addNameComponent(Type.SURNAME, getStringValue(resultSet, fields.get(3)));
    name.addNameComponent(Type.ADD_NAME, getStringValue(resultSet, fields.get(4)));
    return name;
  }

}
