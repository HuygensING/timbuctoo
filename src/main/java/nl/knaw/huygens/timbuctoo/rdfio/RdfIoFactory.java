package nl.knaw.huygens.timbuctoo.rdfio;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.filestorage.dto.CachedLog;

import javax.ws.rs.core.MediaType;
import java.io.OutputStream;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface RdfIoFactory {
  RdfParser makeRdfParser(CachedLog log);

  RdfSerializer makeRdfSerializer(OutputStream output);

  RdfPatchSerializer makeRdfPatchSerializer(OutputStream output);

  boolean isRdfTypeSupported(MediaType mediaType);
}
