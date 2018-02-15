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

  DESCRIPTION("description", 3),
  CAPABILITYLIST("capabilitylist", 2),
  RESOURCELIST("resourcelist", 1),
  RESOURCEDUMP("resourcedump", 1),
  RESOURCEDUMP_MANIFEST("resourcedump-manifest", 0),
  CHANGELIST("changelist", 1),
  CHANGEDUMP("changedump", 1),
  CHANGEDUMP_MANIFEST("changedump-manifest", 0);

  public static Capability forString(String xmlValue) throws IllegalArgumentException {
    String name = xmlValue.toUpperCase().replace('-', '_');
    return valueOf(name);
  }

  public static int levelfor(String xmlValue) {
    String name = xmlValue.toUpperCase().replace('-', '_');
    try {
      return valueOf(name).level;
    } catch (IllegalArgumentException e) {
      return -1;
    }
  }

  public final String xmlValue;
  public final int level;

  Capability(String xmlValue, int level) {
    this.xmlValue = xmlValue;
    this.level = level;
  }

  public String getXmlValue() {
    return xmlValue;
  }

  public int getLevel() {
    return level;
  }

  public String getFilename() {
    return String.format("%s.xml", xmlValue);
  }

  /**
   * The capability of a parent document expressed with a link with relation type 'up'. Except for documents with
   * capability 'description' such a link is mandatory.
   * @return the capability of a parent document with relation type 'up', or <code>null</code> if such a
   *     relation does not exist.
   */
  public Capability getUpRelation() {
    if (this == DESCRIPTION) {
      return null;
    } else if (this == CAPABILITYLIST) {
      return DESCRIPTION;
    } else {
      return CAPABILITYLIST;
    }
  }

  /**
   * The capability of a parent document expressed with a link with relation type 'index'.
   * @return the capability of a parent document with relation type 'index', or <code>null</code> if such a
   *     relation does not exist.
   */
  public Capability getIndexRelation() {
    if (this == RESOURCEDUMP_MANIFEST || this == CHANGEDUMP_MANIFEST) {
      return null;
    } else {
      return this;
    }
  }

  /**
   * The capability of possible child documents as expressed in the &lt;loc&gt; element of &lt;url&gt; and
   * &lt;sitemap&gt;.
   * @return an Array of Capabilities of possible child documents.
   */
  public Capability[] getChildRelations() {
    // child relations as expressed in de <loc> element of <url> and <sitemap>.
    // 'this' only allowed if relation from index; index can have children with same capability.
    // manifest files are related to by means of <rs:ln> element with type 'contents'.
    if (this == DESCRIPTION) {
      return new Capability[]{this, CAPABILITYLIST};
    } else if (this == CAPABILITYLIST) {
      return new Capability[]{this, RESOURCELIST, RESOURCEDUMP, CHANGELIST, CHANGEDUMP};
    } else if (this == RESOURCELIST || this == RESOURCEDUMP || this == CHANGELIST || this == CHANGEDUMP) {
      return new Capability[]{this};
    } else {
      return new Capability[]{};
    }
  }

  public String[] getChildRelationsXmlValues() {
    Capability[] childRelations = getChildRelations();
    String[] xmlValues = new String[childRelations.length];
    for (int i = 0; i < childRelations.length; i++) {
      xmlValues[i] = childRelations[i].xmlValue;
    }
    return xmlValues;
  }

  public boolean verifyUpRelation(Capability relation) {
    return this.getUpRelation() == relation;
  }

  public boolean verifyIndexRelation(Capability relation) {
    return  this.getIndexRelation() == relation;
  }

  public boolean verifyChildRelation(Capability relation) {
    boolean allowed = false;
    Capability[] childRelations = getChildRelations();
    if (relation == null && childRelations.length == 0) {
      allowed = true;
    } else {
      for (Capability capa : childRelations) {
        if (capa == relation) {
          allowed = true;
          break;
        }
      }
    }
    return allowed;
  }

}
