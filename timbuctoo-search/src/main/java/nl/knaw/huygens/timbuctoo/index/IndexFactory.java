package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface IndexFactory {

  Index createIndexFor(Class<? extends DomainEntity> type, String name);

}
