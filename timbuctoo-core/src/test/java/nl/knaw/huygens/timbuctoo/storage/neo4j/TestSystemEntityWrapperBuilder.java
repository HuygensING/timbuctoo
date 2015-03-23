package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.model.util.Change;
import test.model.TestSystemEntityWrapper;

public class TestSystemEntityWrapperBuilder {
  private String id;
  private int revision;
  private Change modified;

  private TestSystemEntityWrapperBuilder() {

  }

  public static TestSystemEntityWrapperBuilder aSystemEntity() {
    return new TestSystemEntityWrapperBuilder();
  }

  public TestSystemEntityWrapper build() {
    TestSystemEntityWrapper testSystemEntityWrapper = new TestSystemEntityWrapper();
    testSystemEntityWrapper.setId(id);
    testSystemEntityWrapper.setRev(revision);
    testSystemEntityWrapper.setModified(modified);

    return testSystemEntityWrapper;

  }

  public TestSystemEntityWrapperBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public TestSystemEntityWrapperBuilder withRev(int revision) {
    this.revision = revision;
    return this;
  }

  public TestSystemEntityWrapperBuilder withModified(Change modified) {
    this.modified = modified;
    return this;
  }
}
