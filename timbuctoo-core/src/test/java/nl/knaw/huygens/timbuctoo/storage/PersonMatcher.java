package nl.knaw.huygens.timbuctoo.storage;

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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Person.Gender;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import test.model.projecta.ProjectAPerson;

public class PersonMatcher<T extends Person> extends CompositeMatcher<T> {
  private PersonMatcher() {
    super();
  }

  public static <V extends Person> PersonMatcher<V> likePerson() {
    return new PersonMatcher<V>();
  }

  public PersonMatcher<T> withGender(Gender gender) {
    this.addMatcher(new PropertyEqualtityMatcher<T, Gender>("gender", gender) {

      @Override
      protected Gender getItemValue(T item) {
        return item.getGender();
      }
    });
    return this;
  }

  public PersonMatcher<T> withId(String id) {
    this.addMatcher(new PropertyEqualtityMatcher<T, String>("id", id) {

      @Override
      protected String getItemValue(T item) {
        return item.getId();
      }
    });
    return this;
  }

  public PersonMatcher<T> withNames(List<PersonName> names) {
    this.addMatcher(new PropertyEqualtityMatcher<T, List<PersonName>>("names", names) {

      @Override
      protected List<PersonName> getItemValue(T item) {
        return item.getNames();
      }
    });
    return this;
  }

  public PersonMatcher<T> withBirthDate(Datable birthDate) {
    this.addMatcher(new PropertyEqualtityMatcher<T, Datable>("birthDate", birthDate) {

      @Override
      protected Datable getItemValue(T item) {
        return item.getBirthDate();
      }
    });
    return this;
  }

  public PersonMatcher<T> withDeathDate(Datable deathDate) {
    this.addMatcher(new PropertyEqualtityMatcher<T, Datable>("deathDate", deathDate) {

      @Override
      protected Datable getItemValue(T item) {
        return item.getDeathDate();
      }
    });
    return this;
  }

  public PersonMatcher<T> withRevision(int revision) {
    this.addMatcher(new PropertyEqualtityMatcher<T, Integer>("rev", revision) {

      @Override
      protected Integer getItemValue(T item) {
        return item.getRev();
      }
    });
    return this;
  }

  public PersonMatcher<T> withDeletedFlag(boolean deleted) {
    this.addMatcher(new PropertyEqualtityMatcher<T, Boolean>("deleted", deleted) {

      @Override
      protected Boolean getItemValue(T item) {
        return item.isDeleted();
      }
    });
    return this;
  }

  public PersonMatcher<T> withoutPID() {
    this.addMatcher(new PropertyMatcher<T, String>(DomainEntity.PID, is(nullValue(String.class))) {

      @Override
      protected String getItemValue(T item) {
        return item.getPid();
      }
    });

    return this;
  }

  public PersonMatcher<T> withPID() {
    this.addMatcher(new PropertyMatcher<T, String>(DomainEntity.PID, is(not(nullValue(String.class)))) {

      @Override
      protected String getItemValue(T item) {
        return item.getPid();
      }
    });

    return this;
  }

  public static ProjectAPersonMatcher likeProjectAPerson() {
    return new ProjectAPersonMatcher();
  }

  public static class ProjectAPersonMatcher extends PersonMatcher<ProjectAPerson> {
    private ProjectAPersonMatcher() {
      super();
    }

    public ProjectAPersonMatcher withProjectAPersonProperty(String projectAPersonProperty) {
      addMatcher(new PropertyEqualtityMatcher<ProjectAPerson, String>("projectAPersonProperty", projectAPersonProperty) {

        @Override
        protected String getItemValue(ProjectAPerson item) {
          return item.getProjectAPersonProperty();
        }
      });

      return this;
    }
  }

}
