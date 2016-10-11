package nl.knaw.huygens.timbuctoo.buck;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.maven.repository.internal.MavenRepositorySystemUtils.newSession;

public class Importer {

  public static void main(String[] args) throws Exception {
    // String[] dependendenciesFiles = new String[] {
    //   "/Users/jauco/Documents/timbuctoo/timbuctoo-instancev4/dependencies",
    //   "/Users/jauco/Documents/timbuctoo/ContractDiff/dependencies",
    //   "/Users/jauco/Documents/timbuctoo/security-client-agnostic/dependencies",
    //   "/Users/jauco/Documents/timbuctoo/timbuctoo-test-services/dependencies",
    //   "/Users/jauco/Documents/timbuctoo/HttpCommand/dependencies"
    // };
    // String ignoreFile = "/Users/jauco/Documents/timbuctoo/buck-importer/ignorefile";
    // String targetLocation = "//buck-importer/thirdparty";
    // String buckroot = "/Users/jauco/Documents/timbuctoo";
    // String[] repositories = new String[] {
    //   "http://repo.maven.apache.org/maven2/",
    //   "https://raw.githubusercontent.com/mbknor/mbknor.github.com/master/m2repo/releases",
    //   "http://maven.huygens.knaw.nl/repository/"
    // };
    CmdLineArgs parsedArgs = parseCommandline(args);
    Set<Dependency> dependencies = readDependencyCoords(parsedArgs.getDependendenciesFiles());

    Set<String> ignores = readIgnores(parsedArgs.getIgnoreFile());
    removeIgnoredDependencies(dependencies, ignores,
      Lists.newArrayList("system", "runtime")
    );
    System.out.println("The following dependencies have been selected for download:");
    dependencies.stream()
      .map(x -> x.getArtifact().toString())
      .sorted()
      .forEach(dependency -> System.out.println("  " + dependency));
    System.out.println("The following dependencies were explicitly ignored:");
    ignores.stream()
      .sorted()
      .forEach(dependency -> System.out.println("  " + dependency));
    //use clean local repo so that all dependencies are resolved from the internet
    //this is needed to be sure that we get an actual url for all dependencies
    File tempRepo = Files.createTempDir();
    List<ResolvedArtifact> artifactsAndDependencies = resolveDependencies(
      dependencies,
      tempRepo.getAbsolutePath(),
      makeRepositories(parsedArgs.getRepositoryURIs())
    );

    Map<String, String> ruleOverrides = readOverrides(parsedArgs.getRuleOverrides());

    List<BuckDownloadRule> rules = generateBuckRules(artifactsAndDependencies, dependencies);
    writeRulesToFileSystem(rules, parsedArgs.getTargetLocation(), parsedArgs.getBuckroot(), ruleOverrides);
    delete(tempRepo);
  }

  private static Map<String, String> readOverrides(String folderLocation) {
    if (folderLocation == null) {
      return new HashMap<>();
    } else {
      Map<String, String> result = new HashMap<>();
      File overrideDir = new File(folderLocation);
      if (!overrideDir.isDirectory()) {
        throw new RuntimeException("--overridedir should be a directory, but " + overrideDir.getAbsolutePath() +
          " is not.");
      }
      for (File packagedir : overrideDir.listFiles()) {
        if (packagedir.isDirectory()) {
          for (File artifactdir : packagedir.listFiles()) {
            if (artifactdir.isDirectory()) {
              for (File ext : artifactdir.listFiles()) {
                String key = packagedir.getName() + ":" + artifactdir.getName() + ":" + ext.getName();
                if (ext.isDirectory()) {
                  for (File classifier : ext.listFiles()) {
                    try {
                      result.put(
                        key + ":" + classifier.getName(),
                        String.join("\n", Files.readLines(classifier, Charset.defaultCharset()))
                      );
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  }
                } else {
                  try {
                    result.put(key, String.join("\n", Files.readLines(ext, Charset.defaultCharset())));
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }
              }
            }
          }
        }
      }

      return result;
    }
  }

  private static ArrayList<RemoteRepository> makeRepositories(List<String> urls) {
    ArrayList<RemoteRepository> result = newArrayList();
    for (int i = 0; i < urls.size(); i++) {
      result.add(new RemoteRepository.Builder("repo_" + i, "default", urls.get(i)).build());
    }

    return result;
  }

  private static Set<Dependency> readDependencyCoords(List<String> filenames) {
    Set<Dependency> result = new HashSet<>();
    for (String filename : filenames) {
      List<String> dependencies = readFile(filename);
      for (String dependency : dependencies) {
        if (dependency.equals("The following files have been resolved:") || dependency.equals("none")) {
          continue;
        }
        String coords = dependency.substring(0, dependency.lastIndexOf(':'));
        String scope = dependency.substring(dependency.lastIndexOf(':') + 1);
        if (scope.equals("compile") || scope.equals("runtime") || scope.equals("import") || scope.equals("test") ||
          scope.equals("provided") || scope.equals("system")) {
          result.add(new Dependency(new DefaultArtifact(coords), scope));
        } else {
          throw new RuntimeException("");
        }
      }
    }
    return result;
  }

  private static Set<String> readIgnores(String filename) {
    if (filename == null) {
      return newHashSet();
    } else {
      return newHashSet(readFile(filename));
    }
  }

  private static void removeIgnoredDependencies(Set<Dependency> dependencies, Set<String> ignores,
                                                List<String> scopesToIgnore) {

    Iterator<Dependency> iter = dependencies.iterator();
    while (iter.hasNext()) {
      Dependency dependency = iter.next();

      if (ignores.contains(dependency.getArtifact().toString()) || scopesToIgnore.indexOf(dependency.getScope()) > -1) {
        iter.remove();
      }
    }
  }

  private static List<ResolvedArtifact> resolveDependencies(Set<Dependency> dependencies, String localRepo,
                                                            final List<RemoteRepository> repositories)
    throws DependencyResolutionException {
    final RepositorySystem system = makeMavenRepositorySystem();
    ProgressReporter reporter = new ProgressReporter(
      (count, bytes) -> System.out.print("Resolving dependencies: " + count + " jars/poms, " + bytes + " kb\r"));
    final DefaultRepositorySystemSession session = makeMavenSession(system, localRepo,
      reporter
    );

    System.out.print("Resolving dependencies: initializing\r");

    List<ArtifactResult> dependencyResult = system.resolveDependencies(
      session,
      new DependencyRequest(new CollectRequest(Lists.newArrayList(dependencies), null, repositories), null)
    ).getArtifactResults();

    System.out.println("Resolving dependencies: done");
    reporter.setSilent();
    return getDependenciesPerArtifact(dependencyResult, artifact -> {
      ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
      descriptorRequest.setArtifact(artifact);
      descriptorRequest.setRequestContext(JavaScopes.RUNTIME);
      descriptorRequest.setRepositories(repositories);
      try {
        return system.readArtifactDescriptor(session, descriptorRequest);
      } catch (ArtifactDescriptorException e) {
        throw new RuntimeException("Could not retrieve artifact dependency information", e);
      }
    });
  }

  private static List<BuckDownloadRule> generateBuckRules(List<ResolvedArtifact> artifactResults,
                                                          Set<Dependency> sourceList) {
    Map<String, BuckDownloadRule> lookup = new HashMap<>();
    List<BuckDownloadRule> result = new ArrayList<>();
    //create a rule for each artifact and store it in a hashmap
    for (ResolvedArtifact artifact : artifactResults) {
      BuckDownloadRule rule = new BuckDownloadRule(artifact.getArtifact(), artifact.getRepository());
      lookup.put(makeMavenCoordsWithoutVersion(artifact.getArtifact()), rule);
      result.add(rule);
    }
    //now that we have a rule for each artifact:
    //loop over each artifact's dependencies and find the rule that was created for them. Add those rules as the
    //rule dependencies
    for (ResolvedArtifact artifact : artifactResults) {
      BuckDownloadRule rule = lookup.get(makeMavenCoordsWithoutVersion(artifact.getArtifact()));
      for (Artifact dependency : artifact.getDependencies()) {
        rule.addDependency(lookup.get(makeMavenCoordsWithoutVersion(dependency)));
      }
    }

    //create a toplevel rule that includes all artifacts (not strictly necessary) but makes including easier
    TopLevelBuckDownloadRule topLevelRule = new TopLevelBuckDownloadRule();
    result.add(topLevelRule);
    for (Dependency dependency : sourceList) {
      topLevelRule.addDependency(lookup.get(makeMavenCoordsWithoutVersion(dependency.getArtifact())));
    }

    return result;
  }

  private static void writeRulesToFileSystem(List<BuckDownloadRule> rules, String targetLocation, String buckroot,
                                             Map<String, String> ruleOverrides)
    throws IOException {
    String writeDir;
    if (!targetLocation.endsWith("/")) {
      targetLocation += "/";
    }
    if (buckroot.endsWith("/")) {
      writeDir = buckroot + targetLocation.substring(2);
    } else {
      writeDir = buckroot + targetLocation.substring(1);
    }
    delete(new File(writeDir));

    for (Map.Entry<File, List<BuckDownloadRule>> entry : groupRulesPerfile(rules, writeDir).entrySet()) {
      writeRuleToFile(targetLocation, entry.getKey(), entry.getValue(), ruleOverrides);
    }
  }

  private static void writeRuleToFile(String targetLocation, File file, List<BuckDownloadRule> rules,
                                      Map<String, String> ruleOverrides)
    throws IOException {
    String contents = "";
    rules.sort(Comparator.comparing(BuckDownloadRule::getVersionlessIdentifier));
    for (BuckDownloadRule buckDownloadRule : rules) {
      String identifier = buckDownloadRule.getVersionlessIdentifier();
      if (ruleOverrides.containsKey(identifier)) {
        contents += ruleOverrides.get(identifier);
      } else {
        contents += buckDownloadRule.makeRules(targetLocation);
      }
    }
    writeFile(file, contents);
  }

  private static Map<File, List<BuckDownloadRule>> groupRulesPerfile(
    List<BuckDownloadRule> rules, String writeDir) {
    Map<File, List<BuckDownloadRule>> rulesPerFile = new HashMap<>();
    for (BuckDownloadRule rule : rules) {
      File buckFile = rule.getBuckFile(writeDir);
      if (!rulesPerFile.containsKey(buckFile)) {
        rulesPerFile.put(buckFile, new ArrayList<>());
      }
      rulesPerFile.get(buckFile).add(rule);
    }
    return rulesPerFile;
  }

  private static void writeFile(File file, String contents) throws IOException {
    new File(file.getParent()).mkdirs();
    final FileOutputStream fop = new FileOutputStream(file);
    if (file.createNewFile()) {
      throw new IOException("File could not be created: " + file.getAbsolutePath());
    }
    byte[] contentInBytes = contents.getBytes();
    fop.write(contentInBytes);
    fop.flush();
    fop.close();
  }

  private static List<ResolvedArtifact> getDependenciesPerArtifact(List<ArtifactResult> dependencyResult,
                                                                   Function<Artifact, ArtifactDescriptorResult>
                                                                     getter) {
    final HashMap<String, Artifact> artifactPerName = new HashMap<>();
    for (ArtifactResult artifactResult : dependencyResult) {
      Artifact artifact = artifactResult.getArtifact();
      artifactPerName.put(makeMavenCoordsWithoutVersion(artifact), artifact);
    }

    final List<ResolvedArtifact> result = new LinkedList<>();
    boolean toggle = false;
    for (int i = 0; i < dependencyResult.size(); i++) {
      final ArtifactResult artifactResult = dependencyResult.get(i);
      toggle = printProgress(dependencyResult.size(), i, toggle);
      Artifact artifact = artifactResult.getArtifact();
      ArtifactDescriptorResult lookupResult = getter.apply(artifact);
      Set<Artifact> dependencies = new HashSet<>(lookupResult.getDependencies().size());
      for (Dependency dependency : lookupResult.getDependencies()) {
        if (!dependency.getScope().equals("test") && !dependency.getScope().equals("provided") &&
          !dependency.getScope().equals("system")) {
          Artifact resolvedVersion =
            artifactPerName.get(makeMavenCoordsWithoutVersion(dependency.getArtifact()));
          if (resolvedVersion != null) {
            //     System.err.println("Dependency " + dependency + " of " + artifact +
            //       " was left out of resolved transitive dependencies");
            // } else {
            dependencies.add(resolvedVersion);
          }
        }
      }
      result.add(new ResolvedArtifact(artifact, lookupResult.getRepository(), dependencies));
    }
    return result;
  }

  private static boolean printProgress(int size, int cur, boolean toggle) {
    System.out.print("[");
    int progress = (cur * 20) / size;
    for (int i = 0; i < 20; i++) {
      if (i < progress) {
        System.out.print("#");
      } else {
        System.out.print(" ");
      }
    }
    System.out.print("] ");
    if (toggle) {
      System.out.print("-");
    } else {
      System.out.print("|");
    }
    System.out.print("\r");
    return !toggle;
  }

  private static DefaultRepositorySystemSession makeMavenSession(RepositorySystem system, String localRepoLocation,
                                                                 ProgressReporter reporter) {
    DefaultRepositorySystemSession session = newSession();

    session.setLocalRepositoryManager(
      system.newLocalRepositoryManager(session, new LocalRepository(new File(localRepoLocation)))
    );
    session.setRepositoryListener(reporter);
    session.setTransferListener(reporter);
    session.setReadOnly();
    return session;
  }

  private static RepositorySystem makeMavenRepositorySystem() {
    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService(TransporterFactory.class, FileTransporterFactory.class);
    locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

    locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
      @Override
      public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
        exception.printStackTrace();
      }
    });

    return locator.getService(RepositorySystem.class);
  }

  private static void delete(File file) throws IOException {
    if (file.isDirectory()) {
      for (File c : file.listFiles()) {
        delete(c);
      }
    }
    if (file.exists()) {
      if (!file.delete()) {
        throw new FileNotFoundException("Failed to delete file: " + file);
      }
    }
  }

  private static List<String> readFile(String filename) {
    LinkedList<String> result = new LinkedList<>();
    BufferedReader br = null;
    try {
      String currentLine;
      br = new BufferedReader(new FileReader(filename));
      while ((currentLine = br.readLine()) != null) {
        String trimmedLine = currentLine.trim();
        if (trimmedLine.length() > 0) {
          result.add(trimmedLine);
        }
      }
    } catch (IOException e) {
      return result;
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return result;
  }

  public static String makeMavenCoordsWithoutVersion(Artifact artifact) {
    String result = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getExtension();
    if (artifact.getClassifier().length() > 0) {
      result += ":" + artifact.getClassifier();
    }
    return result;
  }

  private static CmdLineArgs parseCommandline(String[] args) throws CmdLineException, IOException {
    CmdLineArgs parsedArgs = new CmdLineArgs();
    CmdLineParser parser = new CmdLineParser(parsedArgs);
    parser.parseArgument(args);

    if (parsedArgs.isShowHelp()) {
      System.out.println("Write buck rules for fetching maven dependencies.");
      System.out.println();
      System.out.println("Usage: java -jar importer.jar [OPTIONS] dependencyFile [dependencyFile...]");
      System.out.println();
      parser.printUsage(System.out);
      System.out.println(
        "dependency files contain lines in the format group:artifact[:extension[:classifier]]:version:scope.\n" +
          "running mvn dependency:list -DexcludeTransitive=true -DoutputFile=<some name> will generate files in the " +
          "correct format\n");
      System.exit(0);
    }

    return parsedArgs;
  }


  private static class ProgressReporter implements RepositoryListener, TransferListener {
    private int resolved;
    private int kbTransferred;
    private final BiConsumer<Integer, Integer> onUpdate;
    private boolean silent;

    private ProgressReporter(BiConsumer<Integer, Integer> onUpdate) {
      this.onUpdate = onUpdate;
    }

    @Override
    public void artifactResolved(RepositoryEvent event) {
      resolved += 1;
      if (!silent) {
        onUpdate.accept(resolved, kbTransferred);
      }
    }

    @Override
    public void metadataResolved(RepositoryEvent event) {
      resolved += 1;
      if (!silent) {
        onUpdate.accept(resolved, kbTransferred);
      }
    }

    @Override
    public void transferProgressed(TransferEvent event) throws TransferCancelledException {
      kbTransferred += event.getDataLength() / 1024;
      if (!silent) {
        onUpdate.accept(resolved, kbTransferred);
      }
    }

    private void setSilent() {
      this.silent = true;
    }

    @Override
    public void artifactDescriptorInvalid(RepositoryEvent event) {}

    @Override
    public void artifactDescriptorMissing(RepositoryEvent event) {}

    @Override
    public void metadataInvalid(RepositoryEvent event) {}

    @Override
    public void artifactResolving(RepositoryEvent event) {}

    @Override
    public void metadataResolving(RepositoryEvent event) {}

    @Override
    public void artifactDownloading(RepositoryEvent event) {}

    @Override
    public void artifactDownloaded(RepositoryEvent event) {}

    @Override
    public void metadataDownloading(RepositoryEvent event) {}

    @Override
    public void metadataDownloaded(RepositoryEvent event) {}

    @Override
    public void artifactInstalling(RepositoryEvent event) {}

    @Override
    public void artifactInstalled(RepositoryEvent event) {}

    @Override
    public void metadataInstalling(RepositoryEvent event) {}

    @Override
    public void metadataInstalled(RepositoryEvent event) {}

    @Override
    public void artifactDeploying(RepositoryEvent event) {}

    @Override
    public void artifactDeployed(RepositoryEvent event) {}

    @Override
    public void metadataDeploying(RepositoryEvent event) {}

    @Override
    public void metadataDeployed(RepositoryEvent event) {}

    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {}

    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {}

    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {}

    @Override
    public void transferSucceeded(TransferEvent event) {}

    @Override
    public void transferFailed(TransferEvent event) {}

  }
}
