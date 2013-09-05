package nl.knaw.huygens.analysis.lucene;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
 * N.B. Factory used in Solr configuration.
 */
public class DiacriticsFilterFactory extends TokenFilterFactory {

  public DiacriticsFilterFactory(Map<String, String> args) {
    super(args);
  }

  @Override
  public TokenStream create(TokenStream input) {
    return new DiacriticsFilter(input);
  }

}
