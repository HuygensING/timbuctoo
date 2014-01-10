package nl.knaw.huygens.timbuctoo.tools.importer;

/*
 * #%L
 * Timbuctoo tools
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
