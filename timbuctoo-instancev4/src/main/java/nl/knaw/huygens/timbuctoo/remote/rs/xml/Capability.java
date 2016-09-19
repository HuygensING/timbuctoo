package nl.knaw.huygens.timbuctoo.remote.rs.xml;

/**
 * A mandatory attribute in all ResourceSync documents.
 * <q>Defined values are resourcelist, changelist, resourcedump, changedump, resourcedump-manifest,
 * changedump-manifest, capabilitylist, and description.</q>
 *
 * @see <a href="http://www.openarchives.org/rs/1.0/resourcesync#DocumentFormats">
 *   http://www.openarchives.org/rs/1.0/resourcesync#DocumentFormats</a>
 */
public enum Capability {

  DESCRIPTION("description"),
  CAPABILITYLIST("capabilitylist"),
  RESOURCELIST("resourcelist"),
  RESOURCEDUMP("resourcedump"),
  RESOURCEDUMP_MANIFEST("resourcedump-manifest"),
  CHANGELIST("changelist"),
  CHANGEDUMP("changedump"),
  CHANGEDUMP_MANIFEST("changedump-manifest");

  public static Capability forString(String xmlValue) {
    String name = xmlValue.toUpperCase().replace('-', '_');
    return Capability.valueOf(name);
  }

  public final String xmlValue;

  Capability(String xmlValue) {
    this.xmlValue = xmlValue;
  }


}
