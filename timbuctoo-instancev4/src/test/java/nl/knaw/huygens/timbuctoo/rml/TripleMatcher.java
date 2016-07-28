package nl.knaw.huygens.timbuctoo.rml;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class TripleMatcher extends TypeSafeDiagnosingMatcher<Triple> {

  private final Node subject;
  private final Node predicate;
  private final Node object;

  public TripleMatcher(Node subject, Node predicate, Node object) {
    this.subject = subject;
    this.predicate = predicate;
    this.object = object;
  }

  public static TripleMatcher likeTriple(Node subject, Node predicate, Node object) {
    return new TripleMatcher(subject, predicate, object);
  }

  @Override
  protected boolean matchesSafely(Triple actual, Description mismatchDescription) {
    boolean subjectMatches = subject.matches(actual.getSubject());
    mismatchDescription.appendText("Triple with");
    if (!subjectMatches) {
      mismatchDescription.appendText(" Subject with value ").appendValue(actual.getSubject());
    }

    boolean predicateMatches = predicate.matches(actual.getPredicate());
    if (!predicateMatches) {
      mismatchDescription.appendText(" Predicate with value ").appendValue(actual.getSubject());
    }

    boolean objectMatches = object.matches(actual.getObject());
    if (!objectMatches) {
      mismatchDescription.appendText(" Object with value ").appendValue(actual.getObject());
    }

    return subjectMatches && predicateMatches && objectMatches;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(subject).appendText(" ")
               .appendValue(predicate).appendText(" ")
               .appendValue(object).appendText(" .");
  }

}
