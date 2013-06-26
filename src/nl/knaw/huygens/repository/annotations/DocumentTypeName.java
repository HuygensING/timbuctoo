package nl.knaw.huygens.repository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation allows explicit specification of the name to be used for
 * identifying a document type in the data store.</p>
 *
 * <p>By default the name is the class name, with words separated with underscores,
 * in lowercase, and pluralized by adding an 's' character. For example:</p>
 * <ul>
 * <li><code>User</code> becomes <code>users</code>.</li>
 * <li><code>Person</code> becomes <code>persons</code>.</li>
 * <li><code>TeiParagraph</code> becomes <code>tei_paragraphs</code>.</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentTypeName {

  /**
   * Returns the document type name.
   */
  String value();
}
