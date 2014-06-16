package nl.knaw.huygens.timbuctoo.vre;

import nl.knaw.huygens.timbuctoo.index.IndexMapCreator;
import nl.knaw.huygens.timbuctoo.index.IndexNameCreator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class VREManagerProvider implements Provider<VREManager> {

  private IndexNameCreator indexNameCreator;
  private IndexMapCreator indexFactory;

  @Inject
  public VREManagerProvider(IndexNameCreator indexNameCreator, IndexMapCreator indexFactory) {
    this.indexNameCreator = indexNameCreator;
    this.indexFactory = indexFactory;

  }

  @Override
  public VREManager get() {
    return VREManager.createInstance(VREManager.VRE_LIST, indexNameCreator, indexFactory);
  }
}
