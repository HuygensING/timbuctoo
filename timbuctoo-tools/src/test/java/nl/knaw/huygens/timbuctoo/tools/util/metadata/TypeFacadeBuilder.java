package nl.knaw.huygens.timbuctoo.tools.util.metadata;

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

import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

public class TypeFacadeBuilder {
  private FieldMapper fieldMapper = new FieldMapper();
  private TypeNameGenerator typeNameGenerator = new TypeNameGenerator();
  private final Class<?> type;

  private TypeFacadeBuilder(Class<?> type) {
    this.type = type;
  }

  public TypeFacadeBuilder withFieldMapper(FieldMapper fieldMapper) {
    this.fieldMapper = fieldMapper;
    return this;
  }

  public TypeFacadeBuilder withTypeNameGenerator(TypeNameGenerator typeNameGenerator) {
    this.typeNameGenerator = typeNameGenerator;
    return this;
  }

  public TypeFacade build() {
    return new TypeFacade(type, fieldMapper, typeNameGenerator);
  }

  public static TypeFacadeBuilder aTypeFacade(Class<?> type) {
    return new TypeFacadeBuilder(type);
  }
}
