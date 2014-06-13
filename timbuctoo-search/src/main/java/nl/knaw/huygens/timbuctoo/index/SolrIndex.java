package nl.knaw.huygens.timbuctoo.index;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.facetedsearch.FacetedSearchException;
import nl.knaw.huygens.facetedsearch.FacetedSearchLibrary;
import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.facetedsearch.model.NoSuchFieldInIndexException;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetedSearchParameters;
import nl.knaw.huygens.solr.AbstractSolrServer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrIndex implements Index {
  protected static final SolrQuery COUNT_QUERY;

  private final SolrInputDocumentCreator solrDocumentCreator;
  private final AbstractSolrServer solrServer;
  private final String name;
  private final FacetedSearchLibrary facetedSearchLibrary;

  static {
    COUNT_QUERY = new SolrQuery();
    COUNT_QUERY.setQuery("*:*");
    COUNT_QUERY.setRows(0);
  }

  public SolrIndex(String name, SolrInputDocumentCreator solrDocumentCreator, AbstractSolrServer solrServer, FacetedSearchLibrary facetedSearchLibrary) {
    this.name = name;
    this.solrDocumentCreator = solrDocumentCreator;
    this.solrServer = solrServer;
    this.facetedSearchLibrary = facetedSearchLibrary;
  }

  @Override
  public void add(List<? extends DomainEntity> variations) throws IndexException {
    updateIndex(variations);

  }

  private void updateIndex(List<? extends DomainEntity> variations) throws IndexException {
    SolrInputDocument document = solrDocumentCreator.create(variations);

    try {
      solrServer.add(document);
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }
  }

  @Override
  public void update(List<? extends DomainEntity> variations) throws IndexException {
    updateIndex(variations);
  }

  @Override
  public void deleteById(String id) throws IndexException {
    try {
      solrServer.deleteById(id);
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }

  }

  @Override
  public void deleteById(List<String> ids) throws IndexException {
    try {
      solrServer.deleteById(ids);
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }

  }

  @Override
  public void clear() throws IndexException {
    try {
      solrServer.deleteByQuery("*:*");
      solrServer.commit();
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }
  }

  @Override
  public long getCount() throws IndexException {
    try {
      SolrDocumentList results = solrServer.search(COUNT_QUERY).getResults();
      return results.getNumFound();
    } catch (SolrServerException e) {
      throw new IndexException(e);
    }
  }

  @Override
  public void commit() throws IndexException {
    try {
      solrServer.commit();
    } catch (SolrServerException e) {
      throw new IndexException(e);
    } catch (IOException e) {
      throw new IndexException(e);
    }

  }

  @Override
  public void close() throws IndexException {
    try {
      this.commit();
    } finally {
      try {
        solrServer.shutdown();
      } catch (SolrServerException e) {
        throw new IndexException(e);
      } catch (IOException e) {
        throw new IndexException(e);
      }
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public <T extends FacetedSearchParameters<T>> FacetedSearchResult search(FacetedSearchParameters<T> searchParameters) throws SearchException {
    try {
      return facetedSearchLibrary.search(searchParameters);
    } catch (NoSuchFieldInIndexException e) {
      throw new SearchException(e);
    } catch (FacetedSearchException e) {
      throw new SearchException(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SolrIndex)) {
      return false;
    }

    SolrIndex other = (SolrIndex) obj;

    return new EqualsBuilder().append(name, other.name)//
        .append(solrDocumentCreator, other.solrDocumentCreator)//
        .append(facetedSearchLibrary, other.facetedSearchLibrary)//
        .append(solrServer, other.solrServer).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name)//
        .append(solrDocumentCreator)//
        .append(facetedSearchLibrary)//
        .append(solrServer).toHashCode();
  }
}
