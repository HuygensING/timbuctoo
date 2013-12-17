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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * A class that transforms a {@code ResultSet} to a List of {@code DomainEntities}.
 *  @param <T> every type used in this class should extend {@code DomainEntity}.
 */
public class GenericResultSetConverter<T extends DomainEntity> {

  private Map<String, List<String>> propertyMapping;
  private Class<T> type;
  private List<Class<? extends Role>> allowedRoles;

  public GenericResultSetConverter(Class<T> type, Map<String, List<String>> propertyMapping, List<Class<? extends Role>> allowedRoles) {
    checkNotNull(propertyMapping);
    checkNotNull(type);
    this.propertyMapping = propertyMapping;
    this.type = type;
    this.allowedRoles = allowedRoles;
  }

  public List<T> convert(ResultSet resultSet) throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException,
      InvocationTargetException {
    List<T> returnValue = new ArrayList<T>();

    List<Field> fields = new ArrayList<Field>();

    getAllFields(fields, type);

    while (resultSet.next()) {

      returnValue.add(createInstance(type, resultSet));
    }

    return returnValue;
  }

  /**
   * This method converts the {@code resultSet} into {@code List} of {@code Roles}. 
   * @param resultSet the {@code ResultSet} to get the data from.
   * @return a {@code List} of {@code Roles}
   * @throws IllegalArgumentException
   * @throws SQLException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  private List<Role> getRoles(ResultSet resultSet) throws IllegalArgumentException, SQLException, InstantiationException, IllegalAccessException {
    List<Role> roles = Lists.newArrayList();

    if (allowedRoles != null) {
      for (final Class<? extends Role> roleClass : allowedRoles) {

        Predicate<String> startsWith = new Predicate<String>() {
          @Override
          public boolean apply(String input) {
            return input.startsWith(roleClass.getSimpleName());
          }
        };

        if (contains(propertyMapping.keySet(), startsWith)) {
          roles.add(createInstance(roleClass, resultSet));
        }

      }
    }

    return roles;
  }

  /**
   * A method that creates an instance of {@code type}.
   * @param type the type that is used to create an instance of. This could be a {@code Role} or a {@code DomainEntity}
   * @param resultSet the {@code ResultSet} that contains the data the instance will be filled with. 
   * @return an instance of {@code type}.
   * @throws SQLException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  private <U> U createInstance(Class<U> type, ResultSet resultSet) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException {
    List<Field> fields = Lists.newArrayList();
    getAllFields(fields, type);

    U instance = type.newInstance();

    for (Field field : fields) {
      String fieldName = getFieldName(type, field);

      if ("roles".equals(fieldName) && allowedRoles != null) {
        field.setAccessible(true);
        field.set(instance, getRoles(resultSet));
      } else if (propertyMapping.containsKey(fieldName)) {
        field.setAccessible(true);

        Object fieldValue = getFieldValue(resultSet, propertyMapping.get(fieldName), field.getType());

        field.set(instance, fieldValue);
      }
    }

    return instance;
  }

  private String getFieldName(Class<?> type, Field field) {
    //Mapped fields for Roles should start with the name of the Role.  
    if (Role.class.isAssignableFrom(type)) {
      return type.getSimpleName() + "." + field.getName();
    }
    return field.getName();
  }

  private <U> boolean contains(Collection<U> set, Predicate<U> predicate) {
    for (U u : set) {
      if (predicate.apply(u)) {
        return true;
      }
    }

    return false;
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
