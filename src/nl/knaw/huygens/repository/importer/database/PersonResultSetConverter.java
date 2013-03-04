package nl.knaw.huygens.repository.importer.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.util.Datable;

public class PersonResultSetConverter implements ResultSetConverter<Person> {
  private Map<String, List<String>> propertyMapping;

  public PersonResultSetConverter(Map<String, List<String>> propertyMapping) {
    this.propertyMapping = propertyMapping;
  }

  public List<Person> convert(ResultSet resultSet) throws SQLException {
    List<Person> persons = new ArrayList<Person>();

    Person person = null;
    String name = null;
    String birthDate = null;
    String deathDate = null;
    while (resultSet.next()) {
      name = getPropertyValue(resultSet, propertyMapping.get("name"));
      birthDate = getPropertyValue(resultSet, propertyMapping.get("birthDate"));
      deathDate = getPropertyValue(resultSet, propertyMapping.get("deathDate"));

      person = new Person();
      person.birthDate = new Datable(birthDate);
      person.deathDate = new Datable(deathDate);
      person.name = name;
      persons.add(person);
    }

    return persons;
  }

  private String getPropertyValue(ResultSet resultSet, Collection<String> columnNames) throws SQLException {
    StringBuilder stringBuilder = new StringBuilder();
    String columnValue = null;
    for (String columnName : columnNames) {
      columnValue = resultSet.getString(columnName);
      if (columnValue != null) {
        stringBuilder.append(columnValue);
        stringBuilder.append(" ");
      }
    }
    return stringBuilder.toString();
  }
}
