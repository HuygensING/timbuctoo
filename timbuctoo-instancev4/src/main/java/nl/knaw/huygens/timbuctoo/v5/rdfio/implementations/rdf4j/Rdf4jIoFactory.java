package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfParser;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;

import java.io.OutputStream;

import static org.eclipse.rdf4j.rio.Rio.getWriterFormatForMIMEType;

public class Rdf4jIoFactory implements RdfIoFactory {
  private static RdfParser rdfParser = new Rdf4jRdfParser();

  private String rdfFormat = "application/n-quads";

  @Override
  public RdfParser makeRdfParser() {
    return rdfParser;
  }

  @Override
  public RdfSerializer makeRdfSerializer(OutputStream output) {
    return new Rdf4jWriter(
      output,
      getWriterFormatForMIMEType(rdfFormat)
        .orElseThrow(() -> new IllegalStateException("Not a known rdf serializer format: " + rdfFormat))
    );
  }

  public String getRdfFormat() {
    return rdfFormat;
  }

  public void setRdfFormat(String rdfFormat) {
    this.rdfFormat = rdfFormat;
  }
}
