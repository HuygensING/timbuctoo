package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
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

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.ClientEntityRepresentation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.google.common.collect.Lists;

public class ClientEntityRepresentationCreator {

  public ClientEntityRepresentationCreator() {}

  public <T extends DomainEntity> List<ClientEntityRepresentation> createRefs(Class<T> type, List<T> entities) {
    String itype = TypeNames.getInternalName(type);
    String xtype = TypeNames.getExternalName(type);
    List<ClientEntityRepresentation> list = Lists.newArrayListWithCapacity(entities.size());
    for (DomainEntity entity : entities) {
      list.add(new ClientEntityRepresentation(itype, xtype, entity));
      // TODO eliminate this, once results are no longer part of the representation
      entity.clearRelations();
    }
    return list;
  }

}
