package nl.knaw.huygens.repository.importer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;

public class SQLImporter {

  protected final String connectionString;
  protected final String userName;
  protected final String password;

  public SQLImporter(String connectionString, String userName, String password) {
    this.connectionString = connectionString;
    this.userName = userName;
    this.password = password;
  }

  public <T extends Document> List<T> executeQuery(String query, ResultSetConverter<T> converter) throws SQLException {
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
