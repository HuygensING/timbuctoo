package nl.knaw.huygens.timbuctoo.v5.graphql.collectionfilter;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface Facet {

  String getCaption();

  List<FacetOption> getOptions();

  static Facet facet(String caption, Iterable<? extends FacetOption> facetOptions) {
    return ImmutableFacet.builder()
      .caption(caption)
      .addAllOptions(facetOptions)
      .build();
  }
}
