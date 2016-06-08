package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;


import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.model.PersonNameComponent.Type.FORENAME;
import static nl.knaw.huygens.timbuctoo.model.PersonNameComponent.Type.GEN_NAME;
import static nl.knaw.huygens.timbuctoo.model.PersonNameComponent.Type.NAME_LINK;
import static nl.knaw.huygens.timbuctoo.model.PersonNameComponent.Type.ROLE_NAME;
import static nl.knaw.huygens.timbuctoo.model.PersonNameComponent.Type.SURNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class PersonNamesExcelDescriptionTest {

  @Test
  public void mapsPersonNamesToCellsCorrectly() {

    final String type = "testType";
    final PersonNames personNames = new PersonNames();
    final PersonName personName1 = new PersonName();
    final PersonName personName2 = new PersonName();
    personName1.getComponents().add(new PersonNameComponent(FORENAME, "Forename1"));
    personName1.getComponents().add(new PersonNameComponent(NAME_LINK, "nameLink1"));
    personName1.getComponents().add(new PersonNameComponent(ROLE_NAME, "roleName1"));
    personName1.getComponents().add(new PersonNameComponent(GEN_NAME, "genName1"));
    personName1.getComponents().add(new PersonNameComponent(SURNAME, "surName1a"));
    personName1.getComponents().add(new PersonNameComponent(SURNAME, "surName1b"));
    personName2.getComponents().add(new PersonNameComponent(FORENAME, "Forename2"));
    personName2.getComponents().add(new PersonNameComponent(SURNAME, "surName2"));
    personNames.list.add(personName1);
    personNames.list.add(personName2);

    ExcelDescription instance = new PersonNamesExcelDescription(personNames, type);

    assertThat(instance.getCols(), equalTo(4));
    assertThat(instance.getRows(), equalTo(6));
    assertThat(instance.getType(), equalTo(type));
    assertThat(instance.getValueDescriptions(), contains("1", "2"));
    assertThat(instance.getValueWidth(), equalTo(2));
    assertThat(instance.getCells(), equalTo(new String[][] {
      {FORENAME.getName(), "Forename1", FORENAME.getName(), "Forename2"},
      {NAME_LINK.getName(), "nameLink1", SURNAME.getName(), "surName2"},
      {ROLE_NAME.getName(), "roleName1", null, null},
      {GEN_NAME.getName(), "genName1", null, null},
      {SURNAME.getName(), "surName1a", null, null},
      {SURNAME.getName(), "surName1b", null, null},
    }));
  }

}
