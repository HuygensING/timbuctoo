package nl.knaw.huygens.repository.facets;

import java.io.Serializable;
import java.util.List;

public class Facet implements Serializable {

	private static final long serialVersionUID = -945661982698717507L;
	private List<FacetItem> facetItems;
	private String indexedName;
	private boolean isComplex;

	public Facet(String indexedName, boolean isComplex) {
	  this.indexedName = indexedName;
	  this.isComplex = isComplex;
	}

	public List<FacetItem> getFacetItems() {
		return facetItems;
	}

	public void setFacetItems(List<FacetItem> facetItems) {
		this.facetItems = facetItems;
	}

	public String getIndexedName() {
		return indexedName;
	}

	public void setIndexedName(String indexedName) {
		this.indexedName = indexedName;
	}

  public boolean isComplex() {
    return isComplex;
  }

  public void setIsComplex(boolean isComplex) {
    this.isComplex = isComplex;
  }
}
