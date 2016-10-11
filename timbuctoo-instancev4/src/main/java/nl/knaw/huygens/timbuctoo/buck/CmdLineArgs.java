package nl.knaw.huygens.timbuctoo.buck;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

class CmdLineArgs {
  @Argument(usage = "Files containing the dependencies coords", metaVar = "dependencyFile")
  private
  List<String> dependendenciesFiles = new ArrayList<>();

  @Option(name = "--root", usage = "The buckroot")
  private
  String buckroot;

  @Option(name = "--ignores", usage = "A file containing the dependencies to ignore")
  private
  String ignoreFile;

  @Option(name = "--targetlocation", usage = "a buckrule containing the target location (i.e. //third-party)")
  private
  String targetLocation = "//third-party";

  @Option(name = "--mavenrepo", usage = "Maven URI(s) (can be specified more then once)")
  private
  List<String> repositoryURIs = new ArrayList<>();

  @Option(name = "--RuleOverrides", usage = "A directory containing rules that should be used instead of what this importer would generate by itself")
  private
  String ruleOverrides;

  @Option(name = "--help", help = true)
  private
  boolean showHelp;

  public List<String> getDependendenciesFiles() {
    return dependendenciesFiles;
  }

  public String getBuckroot() {
    return buckroot;
  }

  public String getIgnoreFile() {
    return ignoreFile;
  }

  public String getTargetLocation() {
    return targetLocation;
  }

  public List<String> getRepositoryURIs() {
    return repositoryURIs;
  }

  public String getRuleOverrides() {
    return ruleOverrides;
  }

  public boolean isShowHelp() {
    return showHelp;
  }
}
