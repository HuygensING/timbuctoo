package nl.knaw.huygens.timbuctoo.vre;

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

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class NotInScopeException extends Exception {
  private NotInScopeException(String message) {
    super(message);
  }

  public static NotInScopeException noTypeMatchesBaseType(Class<? extends DomainEntity> baseType) {
    return new NotInScopeException(String.format("No matching type for base type [%s].", TypeNames.getExternalName(baseType)));
  }

  public static NotInScopeException typeIsNotInScope(Class<? extends DomainEntity> type, String vreId) {
    return new NotInScopeException(String.format("%s is not in the scope of %s", TypeNames.getExternalName(type), vreId));
  }
}
