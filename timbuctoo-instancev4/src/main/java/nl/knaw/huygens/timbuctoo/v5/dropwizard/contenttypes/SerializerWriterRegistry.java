package nl.knaw.huygens.timbuctoo.v5.dropwizard.contenttypes;

import io.dropwizard.jersey.setup.JerseyEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.SupportedExportFormats;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.lang.String.format;

public class SerializerWriterRegistry implements SupportedExportFormats {
  private final JerseyEnvironment jersey;
  private HashSet<String> supportedMimeTypes;

  public SerializerWriterRegistry(JerseyEnvironment jersey) {
    supportedMimeTypes = new LinkedHashSet<>();
    this.jersey = jersey;
  }


  @Override
  public Set<String> getSupportedMimeTypes() {
    return supportedMimeTypes;
  }

  public void register(SerializerWriter serializerWriter) {
    String mimeType = serializerWriter.getMimeType();
    boolean addSucceeded = supportedMimeTypes.add(mimeType);
    if (!addSucceeded) {
      throw new RuntimeException(format("Timbuctoo supports only one serializer writer for '%s'", mimeType));
    }

    jersey.register(serializerWriter);
  }
}
