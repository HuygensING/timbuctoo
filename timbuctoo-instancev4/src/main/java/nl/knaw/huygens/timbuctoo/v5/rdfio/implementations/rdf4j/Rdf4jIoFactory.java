package nl.knaw.huygens.timbuctoo.v5.rdfio.implementations.rdf4j;

import nl.knaw.huygens.timbuctoo.v5.filestorage.dto.CachedLog;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfParser;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;

import javax.ws.rs.core.MediaType;
import java.io.OutputStream;
import java.util.Set;

import static org.eclipse.rdf4j.rio.Rio.getWriterFormatForMIMEType;

public class Rdf4jIoFactory implements RdfIoFactory {
  private static final MediaType RDF_PATCH_TYPE = new MediaType("application", "rdf-patch");
  public static final String RDFP_EXTENSION = ".rdfp";
  public static final Set<RDFFormat> RDF_FORMATS = RDFParserRegistry.getInstance().getKeys();
  private static RdfParser rdfParser = new Rdf4jRdfParser();
  private static RdfParser rdfPatchParser = new Rdf4jRdfPatchParser();

  private String rdfFormat = "application/n-quads";

  @Override
  public RdfParser makeRdfParser(CachedLog log) {
    if (log.getMimeType().isPresent() && log.getMimeType().get().equals(RDF_PATCH_TYPE) ||
      log.getName().toString().endsWith(RDFP_EXTENSION)) {
      return rdfPatchParser;
    } else {
      return rdfParser;
    }
  }

  @Override
  public RdfSerializer makeRdfSerializer(OutputStream output) {
    return new Rdf4jWriter(
      output,
      getWriterFormatForMIMEType(rdfFormat)
        .orElseThrow(() -> new IllegalStateException("Not a known rdf serializer format: " + rdfFormat))
    );
  }

  @Override
  public boolean isRdfTypeSupported(MediaType mediaType) {
    if (mediaType == null) {
      return false;
    } else if (RDF_PATCH_TYPE.equals(mediaType)) {
      return true;
    }

    return RDF_FORMATS.stream().anyMatch(format -> format.hasMIMEType(mediaType.toString()));
  }

  public String getRdfFormat() {
    return rdfFormat;
  }

  public void setRdfFormat(String rdfFormat) {
    this.rdfFormat = rdfFormat;
  }
}
