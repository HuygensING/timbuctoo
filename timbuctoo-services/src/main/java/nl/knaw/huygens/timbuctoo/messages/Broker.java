package nl.knaw.huygens.timbuctoo.messages;

/*
 * #%L
 * Timbuctoo services
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

import javax.jms.JMSException;

public interface Broker {

  String BROKER_NAME = "repo-broker";
  String INDEX_QUEUE = "index";
  String PERSIST_QUEUE = "persist";
  // Message headers
  String PROP_ACTION = "action";
  String PROP_DOC_TYPE = "type";
  String PROP_DOC_ID = "id";

  /**
   * Returns the message producer with the specified name that writes
   * messages to the specified queue, creating it if it does not exist.
   */
  Producer getProducer(String name, String queue) throws JMSException;

  /**
   * Returns the message consumer with the specified name that reads
   * messages from the specified queue, creating it if it does not exist.
   */
  Consumer getConsumer(String name, String queue) throws JMSException;

  Browser newBrowser(String queue) throws JMSException;

  void start() throws JMSException;

  void close();

}
