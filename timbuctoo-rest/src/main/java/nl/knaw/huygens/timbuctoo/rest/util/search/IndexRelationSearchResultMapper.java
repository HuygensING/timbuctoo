package nl.knaw.huygens.timbuctoo.rest.util.search;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

public class IndexRelationSearchResultMapper extends RelationSearchResultMapper {

  public IndexRelationSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, RelationMapper relationMapper, VRECollection vreCollection) {
    super(repository, sortableFieldFinder, hateoasURICreator, relationMapper, vreCollection);
  }
}
