package nl.knaw.huygens.timbuctoo.remote.rs.sync;

import java.net.URI;

/**
 * Describes a set of resources.
 *
 * <q>
 *   Since a Source has one Capability List per set of resources that it distinguishes,
 *   the Source Description will enumerate as many Capability Lists as the Source has distinct sets of resources.
 * </q>
 * from <a href="http://www.openarchives.org/rs/1.0/resourcesync#SourceDesc">
 *   http://www.openarchives.org/rs/1.0/resourcesync#SourceDesc</a>
 *
 * So effectively this class represents the xml-structure of the element &lt;url&gt; of a document with
 * capability 'description'.
 *
 */
public class ResourceSet {

  private URI parentDocumentUri;

  public ResourceSet(URI parentDocumentUri) {
    this.parentDocumentUri = parentDocumentUri;
  }


}
