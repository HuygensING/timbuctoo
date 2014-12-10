package nl.knaw.huygens.timbuctoo.model.ckcc;

/*
 * #%L
 * Timbuctoo model
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.FloruitPeriod;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;

import org.junit.Before;
import org.junit.Test;

public class CKCCPersonTest {

  private CKCCPerson person;

  @Before
  public void createPerson() {
    person = new CKCCPerson();
    person.addName(PersonName.newInstance("forename", "surname"));
  }

  private void setBirthAndDeath(String birth, String death) {
    person.setBirthDate(new Datable(birth));
    person.setDeathDate(new Datable(death));
  }

  private void setFloruit(String startDate, String endDate) {
    person.setFloruit(new FloruitPeriod(startDate, endDate));
  }

  @Test
  public void testNameWithBirthAndDeath() {
    setBirthAndDeath("1568-01-01", "1648-01-01");
    assertThat(person.getIdentificationName(), is(equalTo("surname, forename (1568-1648)")));
  }

  @Test
  public void testNameWithBirth() {
    setBirthAndDeath("1568?", "");
    assertThat(person.getIdentificationName(), is(equalTo("surname, forename (1568-)")));
  }

  @Test
  public void testNameWithDeath() {
    setBirthAndDeath("", "1648~");
    assertThat(person.getIdentificationName(), is(equalTo("surname, forename (-1648)")));
  }

  @Test
  public void testNameWithBirthAndDeathBC() {
    setBirthAndDeath("287 BC", "212 BC");
    assertThat(person.getIdentificationName(), is(equalTo("surname, forename (287 BC-212 BC)")));
  }

  @Test
  public void testNameWithFloruitRange() {
    setFloruit("1568", "1648");
    assertThat(person.getIdentificationName(), is(equalTo("surname, forename (fl. 1568-1648)")));
  }

  @Test
  public void testNameWithFloruitYear() {
    setFloruit("1568", "1568");
    assertThat(person.getIdentificationName(), is(equalTo("surname, forename (fl. 1568)")));
  }

  @Test
  public void testNameWithBirthDeathAndFloruit() {
    setBirthAndDeath("1568-01-01", "1648-01-01");
    setFloruit("1600", "1600");
    assertThat(person.getIdentificationName(), is(equalTo("surname, forename (1568-1648)")));
  }

}
