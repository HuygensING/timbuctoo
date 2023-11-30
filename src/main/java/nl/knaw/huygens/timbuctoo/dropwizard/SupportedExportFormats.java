package nl.knaw.huygens.timbuctoo.dropwizard;

import java.util.Set;

public interface SupportedExportFormats {
  Set<String> getSupportedMimeTypes();
}
