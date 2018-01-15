package nl.knaw.huygens.timbuctoo.util;

/**
 * Wrapper around checked exceptions. Checked Exceptions (CE's) are not allowed to traverse lambda boundaries.
 * By degrading CE's to RuntimeExceptions we somehow obscure 'normal' program flow.
 * It is therefore good practice that code introducing LambdaOriginatedExceptions should also handle them within a
 * single try/catch block. Handling in this sense means unwrapping the CE's and either handle them or rethrow them
 * as CE.
 */
public class LambdaOriginatedException extends RuntimeException {

  public LambdaOriginatedException(Throwable cause) {
    super(cause);
  }

}
