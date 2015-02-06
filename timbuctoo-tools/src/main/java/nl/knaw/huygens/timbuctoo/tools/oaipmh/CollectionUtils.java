package nl.knaw.huygens.timbuctoo.tools.oaipmh;

/*
 * #%L
 * Timbuctoo tools
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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class CollectionUtils {
  public static String getSingularNameOfBaseCollection(DomainEntity domainEntity) {
    Class<? extends DomainEntity> baseType = TypeRegistry.toBaseDomainEntity(domainEntity.getClass());
    return baseType.getSimpleName().toLowerCase();
  }

  public static String getPluralOfBaseCollection(DomainEntity domainEntity) {
    return String.format("%ss", getSingularNameOfBaseCollection(domainEntity));
  }
}
