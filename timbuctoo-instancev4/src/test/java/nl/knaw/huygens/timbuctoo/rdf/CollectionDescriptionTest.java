package nl.knaw.huygens.timbuctoo.rdf;

import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.rdf.CollectionDescription.RDF_URI_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;


public class CollectionDescriptionTest {

  @Test
  public void equalsReturnsTrueIfThePropertiesAreEqual() {
    CollectionDescription sameDescription1 = new CollectionDescription("entityTypeName", "vreName");
    CollectionDescription sameDescription2 = new CollectionDescription("entityTypeName", "vreName");
    CollectionDescription otherTypeNameDescription = new CollectionDescription("otherTypeName", "vreName");
    CollectionDescription otherVreNameDescription = new CollectionDescription("entityTypeName", "otherVreName");

    assertThat(sameDescription1, is(equalTo(sameDescription2)));
    assertThat(sameDescription1, not(is(equalTo(otherTypeNameDescription))));
    assertThat(sameDescription1, not(is(equalTo(otherVreNameDescription))));
  }

  @Test
  public void getRdfUriPrefixesTheEntityTypeNameWhenTheRdfUriIsNotSetExplicitly() {
    CollectionDescription collectionDescription = new CollectionDescription("entityTypeName", "vreName");

    assertThat(collectionDescription.getRdfUri(), allOf(
      startsWith(RDF_URI_PREFIX),
      endsWith(collectionDescription.getEntityTypeName())));
  }

  @Test
  public void getRdfUriReturnsTheSetRdfUri() {
    String rdfUri = "rdfUri";
    CollectionDescription collectionDescription = new CollectionDescription("entityTypeName", "vreName", rdfUri);

    assertThat(collectionDescription.getRdfUri(), is(rdfUri));
  }

}
