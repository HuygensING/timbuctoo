package nl.knaw.huygens.timbuctoo.rest.config;

import nl.knaw.huygens.timbuctoo.index.IndexMapCreator;
import nl.knaw.huygens.timbuctoo.index.IndexNameCreator;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

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
