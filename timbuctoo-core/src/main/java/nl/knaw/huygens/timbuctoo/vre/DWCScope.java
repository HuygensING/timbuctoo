package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

public class DWCScope extends AbstractScope {

  public DWCScope() throws IOException {
    super("timbuctoo.model.dwcbia");
  }

  @Override
  public String getId() {
    return "dwc";
  }

  @Override
  public String getName() {
    return "DWC";
  }

}
