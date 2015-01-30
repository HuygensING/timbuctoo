package nl.knaw.huygens.timbuctoo.storage.mongo;

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
