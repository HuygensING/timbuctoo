package nl.knaw.huygens.timbuctoo.model.vre;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import static nl.knaw.huygens.timbuctoo.model.vre.Vre.COLOR_CODE_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.DESCRIPTION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.PROVENANCE_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.VRE_LABEL_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.vre.Vre.VRE_NAME_PROPERTY_NAME;

public class VreMetadata {
  private String provenance;
  private String colorCode;
  private String description;
  private String label;


  public String getProvenance() {
    return provenance;
  }

  public void setProvenance(String provenance) {
    this.provenance = provenance;
  }

  public String getColorCode() {
    return colorCode;
  }

  public void setColorCode(String colorCode) {
    this.colorCode = colorCode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void updateVreVertex(Vertex vreVertex) {
    if (provenance != null) {
      vreVertex.property(Vre.PROVENANCE_PROPERTY_NAME, provenance);
    }
    if (label != null) {
      vreVertex.property(VRE_LABEL_PROPERTY_NAME, label);
    }
    if (description != null) {
      vreVertex.property(Vre.DESCRIPTION_PROPERTY_NAME, description);
    }
    if (colorCode != null) {
      vreVertex.property(Vre.COLOR_CODE_PROPERTY_NAME, colorCode);
    }
  }

  static VreMetadata fromVertex(Vertex vreVertex) {
    final VreMetadata instance = new VreMetadata();

    instance.setLabel(vreVertex.property(VRE_LABEL_PROPERTY_NAME).isPresent() ?
      vreVertex.value(VRE_LABEL_PROPERTY_NAME) : vreVertex.value(VRE_NAME_PROPERTY_NAME));

    instance.setColorCode(vreVertex.property(COLOR_CODE_PROPERTY_NAME).isPresent() ?
      vreVertex.value(COLOR_CODE_PROPERTY_NAME) : null);

    instance.setProvenance(vreVertex.property(PROVENANCE_PROPERTY_NAME).isPresent() ?
      vreVertex.value(PROVENANCE_PROPERTY_NAME) : null);

    instance.setDescription(vreVertex.property(DESCRIPTION_PROPERTY_NAME).isPresent() ?
      vreVertex.value(DESCRIPTION_PROPERTY_NAME) : null);

    return instance;
  }


  @Override
  public String toString() {
    return "VreMetadataUpdate{" +
      "provenance='" + provenance + '\'' +
      ", colorCode='" + colorCode + '\'' +
      ", description='" + description + '\'' +
      ", label='" + label + '\'' +
      '}';
  }
}
