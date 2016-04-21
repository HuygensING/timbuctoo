package nl.knaw.huygens.timbuctoo.server.healthchecks;

import com.codahale.metrics.health.HealthCheck;

import java.security.MessageDigest;

public class EncryptionAlgorithmHealthCheck extends HealthCheck {
  private final String encryptionAlgorithm;

  public EncryptionAlgorithmHealthCheck(String encryptionAlgorithm) {
    this.encryptionAlgorithm = encryptionAlgorithm;
  }

  @Override
  protected Result check() throws Exception {
    try {
      MessageDigest.getInstance(encryptionAlgorithm);
      return Result.healthy();
    } catch (Exception ex) {
      return Result.unhealthy("Encyption algorithm \"%s\" is not available", encryptionAlgorithm);
    }
  }
}
