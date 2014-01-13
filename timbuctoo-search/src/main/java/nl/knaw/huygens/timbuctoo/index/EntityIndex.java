package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo search
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

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Represents a Lucene index.
 */
public interface EntityIndex<T extends Entity> {

  void add(Class<T> entityType, String entityId) throws IndexException;

  void modify(Class<T> entityType, String entityId) throws IndexException;

  void remove(String entityId) throws IndexException;

  /**
   * Remove multiple entries from the index.
   * 
   * @param entityIds the id's of of the entries to remove
   * @throws IndexException encapsulates the exceptions generated while deleting.
   */
  void remove(List<String> entityIds) throws IndexException;

  void removeAll() throws IndexException;

  void flush() throws IndexException;

  QueryResponse search(Class<T> entityType, SolrQuery query) throws IndexException;

}
