package nl.knaw.huygens.timbuctoo.rest.resources;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.Paths;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRegularSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.IndexRelationSearchResultMapper;
import nl.knaw.huygens.timbuctoo.rest.util.search.SearchRequestValidator;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import javax.ws.rs.Path;

import static nl.knaw.huygens.timbuctoo.config.Paths.SEARCH_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.VERSION_PARAM;

@Path("{" + VERSION_PARAM + " : " + Paths.V2_1_PATH + "}" + SEARCH_PATH)
public class SearchResourceV2_1 extends SearchResourceV1 {
  @Inject
  public SearchResourceV2_1(TypeRegistry registry, Repository repository, Configuration config, SearchRequestValidator searchRequestValidator, RelationSearcher relationSearcher, IndexRegularSearchResultMapper regularSearchResultMapper, IndexRelationSearchResultMapper relationSearchResultMapper, VRECollection vreCollection) {
    super(registry, repository, config, searchRequestValidator, relationSearcher, regularSearchResultMapper, relationSearchResultMapper, vreCollection);
  }
}
