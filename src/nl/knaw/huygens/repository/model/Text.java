package nl.knaw.huygens.repository.model;

import java.util.List;

import nl.knaw.huygens.repository.model.storage.GenericDBRef;
import nl.knaw.huygens.repository.model.storage.RelatedDocument;
import nl.knaw.huygens.repository.model.storage.Storage;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

@RelatedDocument(type = Person.class, accessors = {"authors", "person"})
public class Text extends Document {
  private List<PersonReference> authors = Lists.newArrayList();
  private String title;

  private List<String> contentTypes = Lists.newArrayList();
  private String period;

  public List<PersonReference> getAuthors() {
    return authors;
  }

  public void setAuthors(List<PersonReference> authors) {
    this.authors = authors;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<String> getContentTypes() {
    return contentTypes;
  }

  public void setContentTypes(List<String> contentTypes) {
    this.contentTypes = contentTypes;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  @Override
  public String getDescription() {
    String authorDesc = "";
    if (authors != null) {
      List<String> authorNames = Lists.newArrayListWithExpectedSize(authors.size());
      for (PersonReference author : authors) {
        if (author.person != null) {
          Person p = author.person.getItem();
          if (p != null) {
            authorNames.add(p.name);
          }
        }
      }
      authorDesc = StringUtils.join(authorNames, ", ");
    }
    authorDesc = StringUtils.isEmpty(authorDesc) ? "(unknown author)" : authorDesc;
    return (StringUtils.isEmpty(title) ? "(unknown title) - " : title + " - ") + authorDesc;
  }

  @Override
  public void fetchAll(Storage storage) {
    if (authors != null) {
      List<GenericDBRef<Person>> personRefs = Lists.newArrayListWithExpectedSize(authors.size());
      for (PersonReference ref : authors) {
        personRefs.add(ref.person);
      }
      if (personRefs.size() > 0) {
        storage.fetchAll(personRefs, Person.class);
      }
    }
  }
}
