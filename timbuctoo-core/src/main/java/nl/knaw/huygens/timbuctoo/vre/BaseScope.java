package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

/**
 * A {@code Scope} with all primitive domain entities.
 */
public class BaseScope extends AbstractScope {

  public BaseScope() throws IOException {
    super("timbuctoo.model");
  }

  @Override
  public String getId() {
    return "base";
  }

  @Override
  public String getName() {
    return "Base Scope";
  }

}
