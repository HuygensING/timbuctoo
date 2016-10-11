package nl.knaw.huygens.timbuctoo.buck;

import java.io.File;
import java.util.Comparator;

class TopLevelBuckDownloadRule extends BuckDownloadRule {
  public TopLevelBuckDownloadRule() {
    super(null, null);
  }

  @Override
  public File getBuckFile(String targetLocation) {
    return new File(targetLocation + "/BUCK");
  }

  @Override
  public String makeRules(String targetLocation) {
    String deps = "";
    dependencies.sort(Comparator.comparing(dep -> dep.getReference(targetLocation)));
    String prevReference = "";
    for (BuckDownloadRule dependency : dependencies) {
      String reference = dependency.getReference(targetLocation);
      if (!prevReference.equals(reference)) {
        deps += "    '" + reference + "',\n";
        prevReference = reference;
      }
    }
    return
      "java_library(\n" +
        "  name = 'all',\n" +
        "  exported_deps = [\n" +
        deps +
        "  ],\n" +
        "  visibility = [\n" +
        "    'PUBLIC'\n" +
        "  ],\n" +
        ")\n\n";
  }

  @Override
  public String getVersionlessIdentifier() {
    return "toplevel";
  }
}
