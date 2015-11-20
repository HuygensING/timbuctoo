package nl.knaw.huygens.timbuctoo.persistence;

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

import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.ConsumerService;
import nl.knaw.huygens.timbuctoo.persistence.request.PersistenceRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jms.JMSException;

public class PersistenceService extends ConsumerService implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(PersistenceService.class);

  private final PersistenceRequestFactory persistenceRequestFactory;

  @Inject
  public PersistenceService(Broker broker, PersistenceRequestFactory persistenceRequestFactory) throws JMSException {
    super(broker, Broker.PERSIST_QUEUE, "PersistenceService");
    this.persistenceRequestFactory = persistenceRequestFactory;
  }

  @Override
  protected void executeAction(Action action) {
    PersistenceRequest persistenceRequest = persistenceRequestFactory.forAction(action);
    persistenceRequest.execute();
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

}
