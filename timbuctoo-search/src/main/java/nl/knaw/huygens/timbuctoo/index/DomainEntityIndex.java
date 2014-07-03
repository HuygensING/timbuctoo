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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.solr.SolrInputDocGenerator;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for talking to a specific index on the Solr Server that matches the
 * class used as a generic parameter. Takes care of converting POJO objects
 * (that extend {@link nl.knaw.huygens.timbuctoo.model.Entity
 * <code>Entity</code>}) to {@link org.apache.solr.common.SolrInputDocument
 * <code>SolrInputDocument</code>}s.
 * 
 * Note that whenever you update entities through this index, it is the
 * caller's responsibility to call
 * {@link nl.knaw.huygens.timbuctoo.index.DomainEntityIndex#flush flush} to
 * update the index and notify the world that this has happened.
 * 
 * @author Gijs
 * 
 * @param <T>
 *          The generic parameter specifying what kind of POJO objects are used,
 *          and (implicitly) which index to index them in.
 */
class DomainEntityIndex<T extends DomainEntity> implements EntityIndex<T> {

  private static final Logger LOG = LoggerFactory.getLogger(DomainEntityIndex.class);

  /**
   * Creates a new {@code DomainEntityIndex} instance.
   */
  public static <U extends DomainEntity> DomainEntityIndex<U> newInstance(Repository repository, LocalSolrServer server, String core) {
    return new DomainEntityIndex<U>(repository, server, core);
  }

  private final Repository repository;
  private final LocalSolrServer solrServer;
  private final String core;
  private final ModelIterator modelIterator;

  /**
   * Creates an indexer for a primitive domain entity.
   */
  private DomainEntityIndex(Repository repository, LocalSolrServer server, String core) {
    this.repository = repository;
    this.solrServer = server;
    this.core = core;
    modelIterator = new ModelIterator();
  }

  /**
   * Adds a {@link nl.knaw.huygens.timbuctoo.model.Entity} to the index.
   */
  @Override
  public void add(Class<T> type, String id) throws IndexException {
    try {
      List<T> variations = repository.getAllVariations(type, id);
      if (!variations.isEmpty()) {
        solrServer.add(core, getSolrInputDocument(variations));
      }
    } catch (Exception e) {
      LOG.error("Failed to add {} {}", type.getSimpleName(), id);
      throw new IndexException(e);
    }
  }

  /**
   * Updates a {@link nl.knaw.huygens.timbuctoo.model.Entity} already in the index.
   */
  @Override
  public void modify(Class<T> type, String id) throws IndexException {
    try {
      List<T> variations = repository.getAllVariations(type, id);
      if (!variations.isEmpty()) {
        solrServer.add(core, getSolrInputDocument(variations));
      }
    } catch (Exception e) {
      LOG.error("Failed to modify {} {}", type.getSimpleName(), id);
      throw new IndexException(e);
    }
  }

  /**
   * Removes a {@link nl.knaw.huygens.timbuctoo.model.Entity} from the index.
   */
  @Override
  public void remove(String id) throws IndexException {
    try {
      solrServer.deleteById(core, id);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  @Override
  public void remove(List<String> ids) throws IndexException {
    try {
      for (String id : ids) {
        solrServer.deleteById(core, id);
      }
      //solrServer.deleteById(core, ids);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Removes all items from the index.
   */
  @Override
  public void removeAll() throws IndexException {
    try {
      solrServer.deleteAll(core);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Commits all changes to the SolrServer. Use responsibly.
   */
  @Override
  public void flush() throws IndexException {
    try {
      solrServer.commit(core);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

  /**
   * Generate a {@link org.apache.solr.common.SolrInputDocument
   * <code>SolrInputDocument</code>} given the POJO object passed.
   * 
   * @param entities
   *          the entity and it's subtypes that you want a SolrInputDocument for.
   * @return the corresponding SolrInputDocument
   */
  private <U extends T> SolrInputDocument getSolrInputDocument(List<U> entities) {
    SolrInputDocument document = null;
    SolrInputDocGenerator indexer = null;
    for (U entity : entities) {
      if (document == null) {
        indexer = new SolrInputDocGenerator(entity);
      } else {
        indexer = new SolrInputDocGenerator(entity, document);
      }
      modelIterator.processClass(indexer, entity.getClass());
      document = indexer.getResult();
    }
    return document;
  }

  @Override
  public QueryResponse search(Class<T> entityType, SolrQuery query) throws IndexException {
    try {
      return solrServer.search(core, query);
    } catch (Exception e) {
      throw new IndexException(e);
    }
  }

}
