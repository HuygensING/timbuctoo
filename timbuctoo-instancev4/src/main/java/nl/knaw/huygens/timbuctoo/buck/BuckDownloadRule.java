package nl.knaw.huygens.timbuctoo.buck;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static nl.knaw.huygens.timbuctoo.buck.Importer.makeMavenCoordsWithoutVersion;

class BuckDownloadRule {
  protected final Artifact artifact;
  private final ArtifactRepository repository;
  protected final List<BuckDownloadRule> dependencies = new ArrayList<>();

  public BuckDownloadRule(Artifact artifact, ArtifactRepository repository) {
    this.artifact = artifact;
    this.repository = repository;
  }

  public void addDependency(BuckDownloadRule dependency) {
    dependencies.add(dependency);
  }

  public String makeRules(String targetLocation) {
    String name = getName();
    String jarName = name + "_jar";
    String jarRemoteName = name + "_remote_jar";

    dependencies.sort(Comparator.comparing(dep -> dep.getReference(targetLocation)));
    String deps = "";
    for (BuckDownloadRule dependency : dependencies) {
      deps += "    '" + dependency.getReference(targetLocation) + "',\n";
    }

    String url = makeUrl();
    String sha1 = createSha1(artifact.getFile());
    if (artifact.getExtension().equals("jar")) {
      return
        "java_library(\n" +
          "  name = '" + name + "',\n" +
          "  visibility = ['PUBLIC'],\n" +
          "  exported_deps = [\n" +
          "    ':" + jarName + "'\n" +
          "  ],\n" +
          ")\n" +
          "prebuilt_jar(\n" +
          "  name = '" + jarName + "',\n" +
          "  binary_jar = ':" + jarRemoteName + "',\n" +
          "  deps = [\n" +
          deps +
          "  ],\n" +
          "  visibility = [\n" +
          "    'PUBLIC'\n" +
          "  ],\n" +
          ")\n" +
          "remote_file(\n" +
          "  out = '" + name + ".jar',\n" +
          "  name = '" + jarRemoteName + "',\n" +
          "  url = '" + url + "',\n" +
          "  sha1 = '" + sha1 + "',\n" +
          ")\n\n";
    } else {
      return
        "java_library(\n" +
          "  name = '" + name + "',\n" +
          "  visibility = ['PUBLIC'],\n" +
          "  exported_deps = [\n" +
          deps +
          "  ],\n" +
          ")\n\n";
    }
  }

  private String getName() {
    String result = artifact.getArtifactId();
    if (artifact.getClassifier().length() > 0) {
      result += "-" + artifact.getClassifier();
    }
    return result;
  }

  public String getReference(String targetLocation) {
    return getFileDir(targetLocation) + ":" + getName();
  }

  private String getFileDir(String targetLocation) {
    return targetLocation + artifact.getGroupId().replace(".", "/");
  }

  public File getBuckFile(String targetLocation) {
    return new File(getFileDir(targetLocation) + "/BUCK");
  }

  private String makeUrl() {
    if (repository instanceof RemoteRepository) {
      String url = ((RemoteRepository) repository).getUrl();
      if (!url.endsWith("/")) {
        url += "/";
      }
      url += artifact.getGroupId().replace('.', '/') + "/";
      url += artifact.getArtifactId() + "/";
      url += artifact.getVersion() + "/";
      url += artifact.getArtifactId();
      url += "-" + artifact.getVersion();
      if (artifact.getClassifier().length() > 0) {
        url += "-" + artifact.getClassifier();
      }
      url += fileExtensionFor(artifact.getExtension());
      return url;
    } else {
      System.err.println("No repo found for " + getName());
      return "UNKNOWN REPO";
    }
    // String result = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getExtension();
    // return "mvn" + result + ":" + artifact.getVersion();
  }

  private String fileExtensionFor(String type) {
    if (type.equals("jar")) {
      return ".jar";
    } else if (type.equals("aar")) {
      return ".aar";
    } else if (type.equals("src")) {
      return "-sources.jar";
    } else {
      return String.format("-%s.jar", type);
    }
  }

  private String createSha1(File file) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      InputStream fis = new FileInputStream(file);
      byte[] buffer = new byte[8192];
      int curByte;
      while ((curByte = fis.read(buffer)) != -1) {
        digest.update(buffer, 0, curByte);
      }
      return printHexBinary(digest.digest()).toLowerCase();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(
        "Can't generate SHA-1 because this installation does not have the required MessageDigest Algorithm");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(
        "Can't generate SHA-1 because the jar file is not available (anymore): " + file.getAbsolutePath());
    } catch (IOException e) {
      throw new RuntimeException("Can't generate SHA-1 because the jar file can't be read: " + file.getAbsolutePath());
    }
  }

  public String getVersionlessIdentifier() {
    return makeMavenCoordsWithoutVersion(this.artifact);
  }
}
