package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.dto.EntityLookup;

import java.net.URI;

public interface RedirectionService {
  void add(URI uriToRedirectTo, EntityLookup entityLookup);

  void init(TransactionEnforcer transactionEnforcer);
}
