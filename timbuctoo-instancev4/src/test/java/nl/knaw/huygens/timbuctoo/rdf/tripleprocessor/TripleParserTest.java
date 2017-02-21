package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.rdf.TripleCreator.createSingleTriple;
import static nl.knaw.huygens.timbuctoo.rdf.TripleCreator.createSingleTripleWithLiteralObject;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TripleParserTest {

  private static final String SUBJECT_URI_NODE = "http://example.com/subject";
  private static final String SUBJECT_BLANK_LABEL = "subject";
  private static final String SUBJECT_BLANK_NODE = "_:" + SUBJECT_BLANK_LABEL;
  private static final String PREDICATE_URI_NODE = "http://example.com/predicate";
  private static final String OBJECT_URI_NODE = "http://example.com/object";
  private static final String OBJECT_BLANK_LABEL = "object";
  private static final String OBJECT_BLANK_NODE = "_:" + OBJECT_BLANK_LABEL;
  private static final String OBJECT_VALUE = "x";
  private static final String OBJECT_TYPE_URI = "http://www.w3.org/2001/XMLSchema#string";
  private static final String OBJECT_LITERAL_NODE = "\"" + OBJECT_VALUE + "\"^^<" + OBJECT_TYPE_URI + ">";

  @Test
  public void getSubjectReferenceReturnsTheSubjectUriIfTheSubjectIsAnUriNode() {
    Triple triple = createSingleTriple(SUBJECT_URI_NODE, PREDICATE_URI_NODE, OBJECT_URI_NODE);
    TripleParser instance = TripleParser.fromTriple(triple);

    String subjectReference = instance.getSubjectReference();

    assertThat(subjectReference, is(SUBJECT_URI_NODE));
  }

  @Test
  public void getSubjectReferenceReturnsTheBlankNodeIfTheSubjectIsABlankNode() {
    Triple triple = createSingleTriple(SUBJECT_BLANK_NODE, PREDICATE_URI_NODE, OBJECT_URI_NODE);
    TripleParser instance = TripleParser.fromTriple(triple);

    String subjectReference = instance.getSubjectReference();

    assertThat(subjectReference, is(SUBJECT_BLANK_LABEL));
  }

  @Test
  public void getPredicateReferenceReturnsThePredicateUri() {
    Triple triple = createSingleTriple(SUBJECT_URI_NODE, PREDICATE_URI_NODE, OBJECT_URI_NODE);
    TripleParser instance = TripleParser.fromTriple(triple);

    String predicateReference = instance.getPredicateReference();

    assertThat(predicateReference, is(PREDICATE_URI_NODE));
  }

  @Test
  public void getObjectReferenceReturnsTheSubjectUriIfTheSubjectIsAnUriNode() {
    Triple triple = createSingleTriple(SUBJECT_URI_NODE, PREDICATE_URI_NODE, OBJECT_URI_NODE);
    TripleParser instance = TripleParser.fromTriple(triple);

    String objectReference = instance.getObjectReference();

    assertThat(objectReference, is(OBJECT_URI_NODE));
  }

  @Test
  public void getObjectReferenceReturnsTheBlankNodeIfTheSubjectIsABlankNode() {
    Triple triple = createSingleTriple(SUBJECT_URI_NODE, PREDICATE_URI_NODE, OBJECT_BLANK_NODE);
    TripleParser instance = TripleParser.fromTriple(triple);

    String objectReference = instance.getObjectReference();

    assertThat(objectReference, is(OBJECT_BLANK_LABEL));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getObjectReferenceThrowsAnExceptionWhenTheObjectIsALiteral() {
    Triple triple = createSingleTripleWithLiteralObject(SUBJECT_URI_NODE, PREDICATE_URI_NODE, OBJECT_LITERAL_NODE);
    TripleParser instance = TripleParser.fromTriple(triple);

    instance.getObjectReference();
  }

  @Test
  public void getObjectAsLiteralReturnsTheValueAndTypeOfTheObject() {
    Triple triple = createSingleTripleWithLiteralObject(SUBJECT_URI_NODE, PREDICATE_URI_NODE, OBJECT_LITERAL_NODE);
    TripleParser instance = TripleParser.fromTriple(triple);

    LiteralLabel value = instance.getObjectAsLiteral();

    assertThat(value, allOf(
      hasProperty("value", is(OBJECT_VALUE)),
      hasProperty("datatype", hasProperty("URI", is(OBJECT_TYPE_URI)))
    ));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getObjectAsLiteralReturnsThrowsAnExceptionWhenTheObjectIsAUri() {
    Triple triple = createSingleTriple(SUBJECT_URI_NODE, PREDICATE_URI_NODE, OBJECT_URI_NODE);
    TripleParser instance = TripleParser.fromTriple(triple);

    instance.getObjectAsLiteral();
  }

  @Test(expected = IllegalArgumentException.class)
  public void getObjectAsLiteralReturnsThrowsAnExceptionWhenTheObjectIsBlank() {
    Triple triple = createSingleTriple(SUBJECT_URI_NODE, PREDICATE_URI_NODE, OBJECT_BLANK_NODE);
    TripleParser instance = TripleParser.fromTriple(triple);

    instance.getObjectAsLiteral();
  }
}
