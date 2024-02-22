package nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.graphql.collectionfilter.FilterResult;

import java.io.IOException;

public interface ConfiguredFilter {
  FilterResult query() throws IOException;
}
