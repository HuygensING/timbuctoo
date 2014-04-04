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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.validation.ValidationException;

public abstract class GenericDataHandler {

  protected static final String QUERY_LABEL = "query";
  protected static final String PASSWORD_LABEL = "password";
  protected static final String USER_NAME_LABEL = "userName";
  protected static final String CONNECTION_STRING_LABEL = "connectionString";

  protected String connectionString;
  protected String userName;
  protected String password;
  protected String query;

  private Map<String, List<String>> objectMapping;

  public <T extends DomainEntity> void importData(String configFile, Class<T> type, List<Class<? extends Role>> allowedRoles, Change change) throws Exception {
    System.out.printf("%n=== Import documents of type '%s'%n", type.getSimpleName());

    readMapping(configFile);
    GenericResultSetConverter<T> converter = new GenericResultSetConverter<T>(type, objectMapping, allowedRoles);
    SQLImporter importer = new SQLImporter(connectionString, userName, password);
    List<T> objects = importer.executeQuery(query, converter);

    save(type, objects, change);
  }

  protected void readMapping(String filePath) throws IOException {
    Properties mapping = new Properties();
    mapping.load(new FileInputStream(filePath));

    objectMapping = new HashMap<String, List<String>>();
    for (Entry<Object, Object> entry : mapping.entrySet()) {
      if (CONNECTION_STRING_LABEL.equals(entry.getKey())) {
        connectionString = (String) entry.getValue();
      } else if (USER_NAME_LABEL.equals(entry.getKey())) {
        userName = (String) entry.getValue();
      } else if (PASSWORD_LABEL.equals(entry.getKey())) {
        password = (String) entry.getValue();
      } else if (QUERY_LABEL.equals(entry.getKey())) {
        query = (String) entry.getValue();
      } else {
        String key = (String) entry.getKey();
        List<String> value = getEntryValue((String) entry.getValue());
        objectMapping.put(key, value);
      }
    }
  }

  private List<String> getEntryValue(String value) {
    List<String> values = new ArrayList<String>();
    for (String valuePart : value.split(",")) {
      values.add(valuePart.trim());
    }
    return values;
  }

  protected abstract <T extends DomainEntity> void save(Class<T> type, List<T> objects, Change change) throws IOException, ValidationException;

}
