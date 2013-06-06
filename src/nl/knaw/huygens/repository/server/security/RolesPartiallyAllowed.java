package nl.knaw.huygens.repository.server.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that is used by a filter that checks which Role has all the access and which on a part of the data. 
 * @author martijnm
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface RolesPartiallyAllowed {
  String[] fullyAllowed() default {};

  /**
   * Users in this group can only access their own data. 
   * For example the user can only retrieve his/her own User object.
   * @return
   */
  String[] ownDataAllowed() default {};
}
