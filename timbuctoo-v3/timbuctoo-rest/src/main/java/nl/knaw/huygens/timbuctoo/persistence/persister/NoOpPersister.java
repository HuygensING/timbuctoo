package nl.knaw.huygens.timbuctoo.persistence.persister;

/*
 * #%L
 * Timbuctoo REST api
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

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NoOpPersister implements Persister {
  Logger LOG = LoggerFactory.getLogger(NoOpPersister.class);

  @Override
  public void execute(DomainEntity domainEntity) {
    LOG.info("Doing nothing for DomainEntity of type \"{}\" with id \"{}\"", domainEntity.getClass(), domainEntity.getId());
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
