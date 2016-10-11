package nl.knaw.huygens.timbuctoo.buck;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.ArtifactRepository;

import java.util.Set;

class ResolvedArtifact {
  private final Artifact artifact;
  private final ArtifactRepository repository;
  private final Set<Artifact> dependencies;

  ResolvedArtifact(Artifact artifact, ArtifactRepository repository, Set<Artifact> dependencies) {
    this.artifact = artifact;
    this.repository = repository;
    this.dependencies = dependencies;
  }

  public Artifact getArtifact() {
    return artifact;
  }

  public ArtifactRepository getRepository() {
    return repository;
  }

  public Set<Artifact> getDependencies() {
    return dependencies;
  }
}
