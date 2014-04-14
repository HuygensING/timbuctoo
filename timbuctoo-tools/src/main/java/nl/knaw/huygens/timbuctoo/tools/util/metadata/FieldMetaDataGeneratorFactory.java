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

import java.lang.reflect.Field;

public class FieldMetaDataGeneratorFactory {
  private final TypeNameGenerator typeNameGenerator;

  public FieldMetaDataGeneratorFactory(TypeNameGenerator typeNameGenerator) {
    this.typeNameGenerator = typeNameGenerator;
  }

  /**
   * Creates a meta data generator for {@code field}.
   * @param field the field where the meta data generator has to be created for.
   * @param containingType the class containing the field.
   * @return the meta data generator of the field.
   */
  public FieldMetaDataGenerator create(Field field, TypeFacade containingType) {
    switch (containingType.getFieldType(field)) {
      case ENUM:
        return new EnumValueFieldMetaDataGenerator(containingType);
      case CONSTANT:
        return new ConstantFieldMetaDataGenerator(containingType);
      case POOR_MANS_ENUM:
        return new PoorMansEnumFieldMetaDataGenerator(containingType, typeNameGenerator);
      case DEFAULT:
        return new DefaultFieldMetaDataGenerator(containingType);
      default:
        return new NoOpFieldMetaDataGenerator(containingType);
    }
  }

}
