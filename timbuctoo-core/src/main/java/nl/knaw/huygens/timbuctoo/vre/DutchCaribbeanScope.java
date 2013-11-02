package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

public class DutchCaribbeanScope extends AbstractScope {

  public DutchCaribbeanScope() throws IOException {
    super("timbuctoo.model.dcar");
  }

  @Override
  public String getId() {
    return "dcar";
  }

  @Override
  public String getName() {
    return "Dutch Caribbean Scope";
  }

}
