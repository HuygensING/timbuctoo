package nl.knaw.huygens.timbuctoo.server.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class PersonNamesDefaultNamePropParserTest extends AbstractPropParserTest {

  private PersonNamesDefaultNamePropParser instance;

  @Before
  public void setUp() throws Exception {
    instance = new PersonNamesDefaultNamePropParser();
  }

  @Test
  public void returnsTheShortNameOfTheFirstNameOfThePersonNames() throws JsonProcessingException {
    PersonNames names = new PersonNames();
    PersonName name1 = PersonName.newInstance("forename", "surname");
    names.list.add(name1);
    names.list.add(PersonName.newInstance("forename2", "surname2"));
    String input = new ObjectMapper().writeValueAsString(names);

    String value = instance.parse(input);

    assertThat(value, is(name1.getShortName()));
  }

  @Test
  public void returnsNullIfTheInputCannotBeParse() throws JsonProcessingException {
    String value = instance.parse("invalid serialized names");

    assertThat(value, is(nullValue()));
  }

  @Override
  protected PropParser getInstance() {
    return instance;
  }
}
