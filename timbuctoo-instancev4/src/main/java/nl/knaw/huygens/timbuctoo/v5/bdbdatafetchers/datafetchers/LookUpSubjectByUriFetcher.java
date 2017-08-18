package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.LazyTypeSubjectReference;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

public class LookUpSubjectByUriFetcher
  implements nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.LookUpSubjectByUriFetcher {
  private final QuadStore quadStore;

  public LookUpSubjectByUriFetcher(QuadStore quadStore) {
    this.quadStore = quadStore;
  }

  @Override
  public SubjectReference getItem(String uri) {
    return new LazyTypeSubjectReference(uri, quadStore);
  }
}
