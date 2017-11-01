package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad.create;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.HAS_VIEW_CONFIG;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StringPredicatesRdfCreatorTest {

  private static final String COLLECTION_URI = "http://example.org/col";
  private static final String BASE_URI = "http://example.org/base";
  private static final String VIEW_CONFIG = "viewConfig";
  private static final String OLD_CONFIG = "oldConfig";
  private RdfPatchSerializer rdfPatchSerializer;
  private StringPredicatesRdfCreator instance;
  private QuadStore quadStore;

  @Before
  public void setUp() throws Exception {
    rdfPatchSerializer = mock(RdfPatchSerializer.class);
    quadStore = mock(QuadStore.class);
  }

  @Test
  public void savesTheConfiguration() throws Exception {
    StringPredicatesRdfCreator instance = new StringPredicatesRdfCreator(
      quadStore,
      ImmutableMap.of(
        Tuple.tuple(COLLECTION_URI, HAS_VIEW_CONFIG), Optional.of(VIEW_CONFIG)
      ),
      BASE_URI
    );
    RdfPatchSerializer rdfPatchSerializer = mock(RdfPatchSerializer.class);

    instance.sendQuads(rdfPatchSerializer);

    verify(rdfPatchSerializer).onQuad(COLLECTION_URI, HAS_VIEW_CONFIG, VIEW_CONFIG, STRING, null, BASE_URI);
  }

  @Test
  public void removesTheOldConfigBeforeAddingTheNewTheConfiguration() throws Exception {
    given(quadStore.getQuads(COLLECTION_URI, HAS_VIEW_CONFIG, Direction.OUT, "")).willReturn(
      newArrayList(create(COLLECTION_URI, HAS_VIEW_CONFIG, Direction.OUT, OLD_CONFIG, STRING, null, "")).stream()
    );
    StringPredicatesRdfCreator instance = new StringPredicatesRdfCreator(
      quadStore,
      ImmutableMap.of(
        Tuple.tuple(COLLECTION_URI, HAS_VIEW_CONFIG), Optional.of(VIEW_CONFIG)
      ),
      BASE_URI
    );

    instance.sendQuads(rdfPatchSerializer);

    InOrder inOrder = inOrder(rdfPatchSerializer);
    inOrder.verify(rdfPatchSerializer).delQuad(COLLECTION_URI, HAS_VIEW_CONFIG, OLD_CONFIG, STRING, null, BASE_URI);
    inOrder.verify(rdfPatchSerializer).onQuad(COLLECTION_URI, HAS_VIEW_CONFIG, VIEW_CONFIG, STRING, null, BASE_URI);
  }

}
