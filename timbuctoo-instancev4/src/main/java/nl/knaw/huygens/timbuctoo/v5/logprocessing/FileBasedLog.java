package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.dto.LocalLog;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfreader.RdfParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Optional;

public class FileBasedLog implements LocalLog {

  private final URI name;
  private final String file;
  private final String contentType;

  public FileBasedLog(URI name, String file, String contentType) {
    this.name = name;
    this.file = file;
    this.contentType = contentType;
  }

  @Override
  public URI getName() {
    return name;
  }

  @Override
  public void loadQuads(RdfParser rdfParser, QuadHandler quadHandler) throws LogProcessingFailedException {
    try {
      rdfParser.importRdf(
        name,
        contentType != null &&  contentType.length() > 0 ? Optional.of(contentType) : Optional.empty(),
        new FileInputStream(file),
        quadHandler
      );
    } catch (FileNotFoundException e) {
      throw new LogProcessingFailedException(e);
    }

  }
}
