package nl.knaw.huygens.timbuctoo.vre;

/**
 * Defines a Virtual Research Environment.
 */
public interface VRE {

  /**
   * Returns the unique name of this VRE.
   */
  String getName();

  /**
   * Returns the {@code Scope} of this VRE.
   * Currently a {@codeVRE} has one {@code Scope}.
   */
  Scope getScope();

}
