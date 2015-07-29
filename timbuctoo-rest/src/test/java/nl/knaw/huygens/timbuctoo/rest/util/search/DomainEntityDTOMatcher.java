package nl.knaw.huygens.timbuctoo.rest.util.search;

public class DomainEntityDTOMatcher extends CompositeM{
  private DomainEntityDTOMatcher() {
  }
  public static DomainEntityDTOMatcher likeDomainEntityDTO() {
    return new DomainEntityDTOMatcher();
  }
}
