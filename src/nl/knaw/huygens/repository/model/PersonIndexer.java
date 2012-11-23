package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.indexdata.CustomIndexer;

import org.apache.solr.common.SolrInputDocument;

public class PersonIndexer implements CustomIndexer {
  @Override
  public void indexItem(SolrInputDocument doc, Object item) {
    try {
      PersonReference ref = (PersonReference) item;
      String name = ref.person.getItem().name;
      doc.addField("facet_s_person", name);
      doc.addField("facet_s_personmap", name + ";;;" + ref.role);
      doc.addField("facet_s_personrole", ref.role);
    } catch (Exception ex) {
      System.err.println("Error indexing person reference: ");
      ex.printStackTrace();
      // No-op;
    }
  }

  @Override
  public String getFieldFilter() {
    return "facet_s_person.*";
  }
}
