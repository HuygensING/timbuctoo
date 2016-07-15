package nl.knaw.huygens.timbuctoo.rdf;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


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

}
