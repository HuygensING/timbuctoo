package nl.knaw.huygens.timbuctoo.security;

import nl.knaw.huygens.timbuctoo.security.localfile.LocalFileLoginAccess;

import java.nio.file.Path;

public class JsonBasedAuthenticatorStubs {
  public static JsonBasedAuthenticator backedByFile(Path file) {
    return new JsonBasedAuthenticator(new LocalFileLoginAccess(file), "SHA-256");
  }

  public static JsonBasedAuthenticator withAlgorithm(Path file, String algorithm) {
    return new JsonBasedAuthenticator(new LocalFileLoginAccess(file), algorithm);
  }
}
