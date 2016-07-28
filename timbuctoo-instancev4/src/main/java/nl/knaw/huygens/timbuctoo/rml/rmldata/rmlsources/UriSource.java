package nl.knaw.huygens.timbuctoo.rml.rmldata.rmlsources;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlSource;

public class UriSource implements RmlSource {
  private final String source;

  public UriSource(String source) {
    this.source = source;
  }

  public String getUri() {
    return source;
  }
}
