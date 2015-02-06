package nl.knaw.huygens.timbuctoo.storage.mongo;

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

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;

public class JacksonDBObjectMatcher extends TypeSafeMatcher<JacksonDBObject<JsonNode>> {

  private final JsonNode expectedObject;

  private JacksonDBObjectMatcher(JsonNode expectedJsonNode) {
    this.expectedObject = expectedJsonNode;
  }

  public static JacksonDBObjectMatcher jacksonDBObjectMatcherHasObject(JsonNode object) {
    return new JacksonDBObjectMatcher(object);
  }

  @Override
  public void describeTo(Description description) {
    describe(description, expectedObject);
  }

  protected void describe(Description description, JsonNode object) {
    description.appendText("JacksonDBObjectMatcher with object: ");
    description.appendValue(object);
  }

  @Override
  protected void describeMismatchSafely(JacksonDBObject<JsonNode> item, Description mismatchDescription) {
    describe(mismatchDescription, item.getObject());
  }

  @Override
  protected boolean matchesSafely(JacksonDBObject<JsonNode> item) {
    return Objects.equal(item.getObject(), expectedObject);
  }

}
