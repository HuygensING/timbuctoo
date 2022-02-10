package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.util.Graph;
import org.eclipse.rdf4j.common.net.ParsedIRI;

import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Creates a SubjectReference from the passed argument instead of reading it from the database
 */
public class LookUpSubjectByUriFetcherWrapper implements DataFetcher {
  private final LookUpSubjectByUriFetcher lookUpSubjectByUriFetcher;

  public LookUpSubjectByUriFetcherWrapper(LookUpSubjectByUriFetcher lookUpSubjectByUriFetcher) {
    this.lookUpSubjectByUriFetcher = lookUpSubjectByUriFetcher;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    try {
      final DatabaseResult source = environment.getSource();
      ParsedIRI baseUri = new ParsedIRI(source.getDataSet().getMetadata().getBaseUri());
      String uri = environment.getArgument("uri");
      String graph = environment.getArgument("graph");
      Optional<Graph> optionalGraph = graph != null ? Optional.of(new Graph(graph)) : Optional.empty();

      return lookUpSubjectByUriFetcher.getItemInGraph(baseUri.resolve(uri), optionalGraph, source.getDataSet());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
