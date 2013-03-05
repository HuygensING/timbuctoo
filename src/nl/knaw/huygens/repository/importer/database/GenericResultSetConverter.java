package nl.knaw.huygens.repository.importer.database;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Datable;

public class GenericResultSetConverter<T extends Document> {
  private Map<String, List<String>> propertyMapping;
  private Class<T> type;

  public GenericResultSetConverter(Map<String, List<String>> propertyMapping, Class<T> type) {
    this.propertyMapping = propertyMapping;
    this.type = type;
  }

  public List<T> convert(ResultSet resultSet) throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException,
      SecurityException, IllegalArgumentException, InvocationTargetException {
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
  
  private <U> void getAllFields(List<Field> fieldList, Class<U> type){
    fieldList.addAll(Arrays.asList(type.getDeclaredFields()));
    if(type.getSuperclass() != null){
      getAllFields(fieldList, type.getSuperclass());
    }
  }

  private <U> Object getFieldValue(ResultSet resultSet, List<String> fieldNames, Class<U> type) throws SQLException {
    Object object = null;

    if (type == String.class) {
      object = getStringValue(resultSet, fieldNames);
    }
    else if(type == Datable.class){
      object = new Datable(resultSet.getString(fieldNames.get(0)));
    }
    else{
      throw new RuntimeException( type.getName() + " not supported yet.");
    }

    return object;
  }

  private String getStringValue(ResultSet resultSet, Collection<String> fieldNames) throws SQLException {
    StringBuilder sb = new StringBuilder();
    String value = null;
    
    for (String field : fieldNames) {
      value = resultSet.getString(field);
      if(value != null)
      sb.append(value);
      sb.append(" ");
    }
    
    return sb.toString().trim();
  }

}
