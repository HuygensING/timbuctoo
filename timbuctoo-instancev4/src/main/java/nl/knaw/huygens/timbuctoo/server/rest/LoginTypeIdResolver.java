package nl.knaw.huygens.timbuctoo.server.rest;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;


public class LoginTypeIdResolver implements TypeIdResolver {


  public static final String TYPENAME = "login";
  public static final JavaType JAVA_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Login.class);

  @Override
  public void init(JavaType baseType) {
  }

  @Override
  public String idFromValue(Object value) {
    return TYPENAME;
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    return TYPENAME;
  }

  @Override
  public String idFromBaseType() {
    return TYPENAME;
  }

  @Override
  public JavaType typeFromId(String id) {
    return JAVA_TYPE;
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {
    return JAVA_TYPE;
  }

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.NAME;
  }
}
