package nl.knaw.huygens.analysis.lucene;

import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.analysis.BaseTokenFilterFactory;

/**
 * N.B. Factory used in Solr configuration.
 */
public class DiacriticsFilterFactory extends BaseTokenFilterFactory {

  @Override
  public TokenStream create(TokenStream input) {
    return new DiacriticsFilter(input);
  }

}
