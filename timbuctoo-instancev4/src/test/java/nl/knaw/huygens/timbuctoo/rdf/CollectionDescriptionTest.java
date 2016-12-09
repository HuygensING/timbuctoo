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

  public static final String ENTITY_TYPE_NAME = "entityTypeName";
  public static final String VRE_NAME = "vreName";

  @Test
  public void equalsReturnsTrueIfThePropertiesAreEqual() {
    CollectionDescription sameDescription1 =
      CollectionDescription.createCollectionDescription(ENTITY_TYPE_NAME, VRE_NAME);
    CollectionDescription sameDescription2 =
      CollectionDescription.createCollectionDescription(ENTITY_TYPE_NAME, VRE_NAME);
    CollectionDescription otherTypeNameDescription =
      CollectionDescription.createCollectionDescription("otherTypeName", VRE_NAME);
    CollectionDescription otherVreNameDescription =
      CollectionDescription.createCollectionDescription(ENTITY_TYPE_NAME, "otherVreName");

    assertThat(sameDescription1, is(equalTo(sameDescription2)));
    assertThat(sameDescription1, not(is(equalTo(otherTypeNameDescription))));
    assertThat(sameDescription1, not(is(equalTo(otherVreNameDescription))));
  }

  @Test
  public void getRdfUriPrefixesTheEntityTypeNameWhenTheRdfUriIsNotSetExplicitly() {
    CollectionDescription collectionDescription =
      CollectionDescription.createCollectionDescription(ENTITY_TYPE_NAME, VRE_NAME);

    assertThat(collectionDescription.getRdfUri(), allOf(
      startsWith(RDF_URI_PREFIX),
      endsWith(collectionDescription.getEntityTypeName())));
  }

  @Test
  public void getRdfUriReturnsTheSetRdfUri() {
    String rdfUri = "rdfUri";
    CollectionDescription collectionDescription =
      CollectionDescription.createCollectionDescription(ENTITY_TYPE_NAME, VRE_NAME, rdfUri);

    assertThat(collectionDescription.getRdfUri(), is(rdfUri));
  }

  @Test
  public void getEntityTypeNameGetsPrefixedWhenItDoesNotStartWithTheVreName() {
    assertThat(CollectionDescription.createCollectionDescription(ENTITY_TYPE_NAME, VRE_NAME).getEntityTypeName(),
      startsWith(VRE_NAME));
    assertThat(
      CollectionDescription.createCollectionDescription(VRE_NAME + ENTITY_TYPE_NAME, VRE_NAME).getEntityTypeName(),
      is(VRE_NAME + ENTITY_TYPE_NAME));
  }

  @Test
  public void createCollectionDescriptionPrefixesTheEntityTypeNameWithTheVre() {
    CollectionDescription collectionDescription = CollectionDescription.createCollectionDescription("person", "vre");

    assertThat(collectionDescription.getEntityTypeName(), is("vreperson"));
  }

  @Test
  public void createCollectionDescriptionDoesNotPrefixTheEntityTypeNameWithTheVreWhenTheVreIsAdmin() {
    CollectionDescription collectionDescription = CollectionDescription.createCollectionDescription("person", "Admin");

    assertThat(collectionDescription.getEntityTypeName(), is("person"));
  }

}
