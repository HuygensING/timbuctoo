package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;

public class VariationException extends IOException {

  private static final long serialVersionUID = 1L;

  public VariationException(String msg) {
    super(msg);
  }

}
