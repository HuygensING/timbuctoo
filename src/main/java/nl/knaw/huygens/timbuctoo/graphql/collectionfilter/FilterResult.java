package nl.knaw.huygens.timbuctoo.graphql.collectionfilter;

import java.util.List;

public interface FilterResult {
  List<String> getUriList();

  String getNextToken();

  int getTotal();

  List<Facet> getFacets();

  String getPrevToken();
}
