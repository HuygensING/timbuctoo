package nl.knaw.huygens.timbuctoo.vre;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexCollection;
import nl.knaw.huygens.timbuctoo.search.RelationSearcher;
import nl.knaw.huygens.timbuctoo.search.converters.SearchResultConverter;

import java.util.List;

public class WomenWritersVRE extends PackageVRE {
  public WomenWritersVRE(String vreId, String description, String modelPackage, List<String> receptions, Repository repository, RelationSearcher relationSearcher) {
    super(vreId, description, modelPackage, receptions, repository, relationSearcher);
  }

  WomenWritersVRE(String vreId, String description, Scope scopeMock, IndexCollection indexCollectionMock, SearchResultConverter resultConverterMock, Repository repositoryMock, RelationSearcher relationSearcher, List<String> receptions) {
    super(vreId, description, scopeMock, indexCollectionMock, resultConverterMock, repositoryMock, relationSearcher, receptions);
  }
}
