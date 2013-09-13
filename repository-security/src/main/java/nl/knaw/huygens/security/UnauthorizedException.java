package nl.knaw.huygens.security;

public class UnauthorizedException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public UnauthorizedException() {
    super("User is not authorized");
  }

  public UnauthorizedException(String message) {
    super(message);
  }

}
