package nl.knaw.huygens.repository.variation;

import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.variation.base.model.TestBaseDoc;

public class TestDoc extends TestBaseDoc {
  public TestDoc() { super(); }

  public String blah;
  @Override
  public String getDescription() {
    return null;
  }
  @Override
  public void fetchAll(Storage storage) {
  }
}