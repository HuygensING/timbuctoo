package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import static nl.knaw.huygens.timbuctoo.tools.oaipmh.VREIdUtils.simplifyVREId;

import java.util.ArrayList;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class CMDIOAIIdentifierGenerator {
  static final String PREFIX = "oai:oaipmh.huygens.knaw.nl"; // arbitrary chosen prefix used by all CMDI-records

  public String generate(DomainEntity domainEntity, String vreId) {

    ArrayList<String> itemsToJoin = Lists.newArrayList(PREFIX, simplifyVREId(vreId), domainEntity.getId());
    return StringUtils.join(itemsToJoin, ":");
  }

}
