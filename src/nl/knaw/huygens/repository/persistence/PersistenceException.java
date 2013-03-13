package nl.knaw.huygens.repository.persistence;

public class PersistenceException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
         
  public PersistenceException(Exception ex){
    super(ex);
  }
  
}
