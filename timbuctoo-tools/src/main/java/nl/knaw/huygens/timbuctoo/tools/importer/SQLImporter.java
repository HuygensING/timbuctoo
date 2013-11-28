package nl.knaw.huygens.timbuctoo.tools.importer;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

/**
 * A class that basicly executes a SQL query and lets a {@code GenericResultSetConverter} create a {@code List} of {@code DomainEntities}.
 */
public class SQLImporter {

  protected final String connectionString;
  protected final String userName;
  protected final String password;

  public SQLImporter(String connectionString, String userName, String password) {
    this.connectionString = connectionString;
    this.userName = userName;
    this.password = password;
  }

  public <T extends DomainEntity> List<T> executeQuery(String query, GenericResultSetConverter<T> converter) throws SQLException, InstantiationException, IllegalAccessException,
      NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
    List<T> returnValue = new ArrayList<T>();

    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      connection = DriverManager.getConnection(this.connectionString, this.userName, this.password);
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      returnValue = converter.convert(resultSet);
    } finally {
      if (resultSet != null) {
        resultSet.close();
      }
      if (statement != null) {
        statement.close();
      }
      if (connection != null) {
        connection.close();
      }
    }

    return returnValue;
  }

}
