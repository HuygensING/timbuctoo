package nl.knaw.huygens.timbuctoo.rml.rmldata;

import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.rmldata.builders.MappingDocumentBuilder;
import org.apache.jena.graph.Triple;

import java.util.List;
import java.util.stream.Stream;

public class RmlMappingDocument {

  private final List<RrTriplesMap> triplesMaps;
  private List<String> errors;

  public RmlMappingDocument(List<RrTriplesMap> triplesMaps, List<String> errors) {
    this.triplesMaps = triplesMaps;
    this.errors = errors;
  }

  public Stream<Triple> execute(ErrorHandler defaultErrorHandler) {
    if (errors.size() > 0) {
      throw new RuntimeException("Mapping contains errors");
    }
    return triplesMaps.stream().flatMap(map -> map.getItems(defaultErrorHandler));
  }

  public static MappingDocumentBuilder rmlMappingDocument() {
    return new MappingDocumentBuilder();
  }

  public List<String> getErrors() {
    return errors;
  }
}
