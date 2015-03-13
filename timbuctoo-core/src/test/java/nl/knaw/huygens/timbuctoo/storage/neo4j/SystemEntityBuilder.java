package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.model.TestSystemEntityWrapper;

public class SystemEntityBuilder {
  private String id;
  private int revision;
  private Change modified;

  private SystemEntityBuilder() {

  }

  public static SystemEntityBuilder aSystemEntity() {
    return new SystemEntityBuilder();
  }

  public TestSystemEntityWrapper build() {
    TestSystemEntityWrapper testSystemEntityWrapper = new TestSystemEntityWrapper();
    testSystemEntityWrapper.setId(id);
    testSystemEntityWrapper.setRev(revision);
    testSystemEntityWrapper.setModified(modified);

    return testSystemEntityWrapper;

  }

  public SystemEntityBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public SystemEntityBuilder withRev(int revision) {
    this.revision = revision;
    return this;
  }

  public SystemEntityBuilder withModified(Change modified) {
    this.modified = modified;
    return this;
  }
}
