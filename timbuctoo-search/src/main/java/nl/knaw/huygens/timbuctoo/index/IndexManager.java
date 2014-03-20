package nl.knaw.huygens.timbuctoo.index;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

public interface IndexManager {

  <T extends DomainEntity> void addEntity(Class<T> type, String id) throws IndexException;

  <T extends DomainEntity> void updateEntity(Class<T> type, String id) throws IndexException;

  <T extends DomainEntity> void deleteEntity(Class<T> type, String id) throws IndexException;

  <T extends DomainEntity> void deleteEntities(Class<T> type, List<String> ids) throws IndexException;

  void deleteAllEntities() throws IndexException;

  <T extends DomainEntity> QueryResponse search(Scope scope, Class<T> type, SolrQuery query) throws IndexException;

  IndexStatus getStatus();

  void commitAll() throws IndexException;

  void close() throws IndexException;

}