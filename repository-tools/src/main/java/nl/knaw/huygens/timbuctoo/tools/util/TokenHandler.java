package nl.knaw.huygens.timbuctoo.tools.util;

/**
 * Performs an action for a specified token.
 */
public interface TokenHandler {

  /**
   * Handles the specified token.
   * @param token the token to handle.
   * @return <code>true</code> if processing is to be continued,
   * <code>false</code> otherwise.
   */
  boolean handle(Token token);

}
