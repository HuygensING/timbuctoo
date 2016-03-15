package nl.knaw.huygens.timbuctoo.storage.graph;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
