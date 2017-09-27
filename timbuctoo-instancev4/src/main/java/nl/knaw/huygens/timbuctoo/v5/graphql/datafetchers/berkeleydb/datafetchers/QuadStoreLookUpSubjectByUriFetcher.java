package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.LookUpSubjectByUriFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

public class QuadStoreLookUpSubjectByUriFetcher implements LookUpSubjectByUriFetcher {
  private final QuadStore quadStore;

  public QuadStoreLookUpSubjectByUriFetcher(QuadStore quadStore) {
    this.quadStore = quadStore;
  }

  @Override
  public SubjectReference getItem(String uri) {
    return new LazyTypeSubjectReference(uri, quadStore);
  }
}
