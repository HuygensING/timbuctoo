package nl.knaw.huygens.timbuctoo.rdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Comparator.comparingInt;

public class UriBearingPersonNamesJsonStringMatcher extends TypeSafeMatcher<String> {

  private final List<PersonNameMatchingRow> personNameMatchingRows;
  private final List<String> errorMessages;
  private final ObjectMapper objectMapper;

  private UriBearingPersonNamesJsonStringMatcher() {
    personNameMatchingRows = Lists.newArrayList();
    errorMessages = Lists.newArrayList();
    objectMapper = new ObjectMapper();
  }

  public static UriBearingPersonNamesJsonStringMatcher matchesPersonNames() {
    return new UriBearingPersonNamesJsonStringMatcher();
  }

  public UriBearingPersonNamesJsonStringMatcher withPersonName(int position, PersonName personName, String uri) {
    personNameMatchingRows.add(new PersonNameMatchingRow(position, personName, uri));
    return this;
  }

  @Override
  protected boolean matchesSafely(String item) {
    try {
      UriBearingPersonNames uriBearingPersonNames = objectMapper.readValue(item, UriBearingPersonNames.class);

      List<PersonName> personNamesList = Lists.newArrayList(uriBearingPersonNames.list);
      Map<String, Integer> nameUris = Maps.newHashMap(uriBearingPersonNames.nameUris);

      personNameMatchingRows.forEach(pmr -> {
        if (personNamesList.size() > pmr.position) {
            if (!personNamesList.get(pmr.position).equals(pmr.personName)) {
              errorMessages.add(format("Does not have '%s' on position '%d' in 'list'.", pmr.personName, pmr.position));
            }
          } else {
            errorMessages.add(format("Does not contain an item at position '%d' of 'list'.", pmr.position));
          }
        if (nameUris.containsKey(pmr.uri)) {
            if (nameUris.get(pmr.uri) != pmr.position) {
              errorMessages.add(format("'%s' in wrong position in nameUris", pmr.uri));
            }
            nameUris.remove(pmr.uri);
          } else {
            errorMessages.add(format("Does not contain item '%s' in 'nameUris'.", pmr.uri));
          }
        }
      );

      nameUris.forEach((key, value) -> {
        errorMessages.add(format("Unexpected name for uri '%s' on position '%d'", key, value));
      });

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return errorMessages.isEmpty();
  }

  @Override
  public void describeTo(Description description) {
    personNameMatchingRows.stream().sorted(comparingInt(pmr -> pmr.position))
                          .forEach(pmr -> description.appendText(pmr.toString()).appendText("\n"));

  }

  @Override
  protected void describeMismatchSafely(String item, Description mismatchDescription) {
    errorMessages.forEach(error -> mismatchDescription.appendText(error).appendText("\n"));
  }

  private static class PersonNameMatchingRow {
    public final int position;
    public final PersonName personName;
    public final String uri;

    PersonNameMatchingRow(int position, PersonName personName, String uri) {
      this.position = position;
      this.personName = personName;
      this.uri = uri;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }
  }
}
