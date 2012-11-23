package nl.knaw.huygens.repository.facets;

import java.io.Serializable;

import org.apache.lucene.queryParser.QueryParser;

public class FacetItem implements Serializable {

	private static final long serialVersionUID = 4338138033985428277L;
	private String id;
	private String label;
	private long count = 0;

	private String solrEscape(String str) {
	  return QueryParser.escape(str).replace(" ", "\\ ");
	}

	public FacetItem(String id, String label, long count) {
		this.setId(solrEscape(id));
		this.label = label;
		this.count = count;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
