package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
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

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Preconditions;

/**
 * Used by Jackson for (de)serialization of entities and roles.
 */
public class TimbuctooTypeIdResolver implements TypeIdResolver {

  private final TypeRegistry typeRegistry;

  public TimbuctooTypeIdResolver() {
    typeRegistry = TypeRegistry.getInstance();
  }

  @Override
  public void init(JavaType baseType) {}

  @Override
  public String idFromValue(Object value) {
    return TypeNames.getInternalName(value.getClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    return TypeNames.getInternalName(suggestedType.getClass());
  }

  @Override
  public String idFromBaseType() {
    return null;
  }

  @Override
  public JavaType typeFromId(String id) {
    Preconditions.checkState(typeRegistry != null, "Type registry not initialized");
    Class<?> token = typeRegistry.getTypeForIName(id);
    if (token == null) {
      token = typeRegistry.getRoleForIName(id);
    }
    return TypeFactory.defaultInstance().uncheckedSimpleType(token);
  }

  @Override
  public Id getMechanism() {
    return Id.NAME;
  }

}
