package nl.knaw.huygens.timbuctoo.storage;

import java.util.List;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
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
    this.addMatcher(new PropertyMatcher<T, Gender>("gender", gender) {

      @Override
      protected Gender getItemValue(T item) {
        return item.getGender();
      }
    });
    return this;
  }

  public PersonMatcher<T> withId(String id) {
    this.addMatcher(new PropertyMatcher<T, String>("id", id) {

      @Override
      protected String getItemValue(T item) {
        return item.getId();
      }
    });
    return this;
  }

  public PersonMatcher<T> withNames(List<PersonName> names) {
    this.addMatcher(new PropertyMatcher<T, List<PersonName>>("names", names) {

      @Override
      protected List<PersonName> getItemValue(T item) {
        return item.getNames();
      }
    });
    return this;
  }

  public PersonMatcher<T> withBirthDate(Datable birthDate) {
    this.addMatcher(new PropertyMatcher<T, Datable>("birthDate", birthDate) {

      @Override
      protected Datable getItemValue(T item) {
        return item.getBirthDate();
      }
    });
    return this;
  }

  public PersonMatcher<T> withDeathDate(Datable deathDate) {
    this.addMatcher(new PropertyMatcher<T, Datable>("deathDate", deathDate) {

      @Override
      protected Datable getItemValue(T item) {
        return item.getDeathDate();
      }
    });
    return this;
  }

  public PersonMatcher<T> withRevision(int revision) {
    this.addMatcher(new PropertyMatcher<T, Integer>("rev", revision) {

      @Override
      protected Integer getItemValue(T item) {
        return item.getRev();
      }
    });
    return this;
  }

  public PersonMatcher<T> withDeletedFlag(boolean deleted) {
    this.addMatcher(new PropertyMatcher<T, Boolean>("deleted", deleted) {

      @Override
      protected Boolean getItemValue(T item) {
        return item.isDeleted();
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
      addMatcher(new PropertyMatcher<ProjectAPerson, String>("projectAPersonProperty", projectAPersonProperty) {

        @Override
        protected String getItemValue(ProjectAPerson item) {
          return item.getProjectAPersonProperty();
        }
      });

      return this;
    }
  }

}
