package nl.knaw.huygens.timbuctoo.annotations;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>This annotation allows explicit specification of the name to be used for
 * identifying an entity type in the data store.</p>
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
public @interface EntityTypeName {

  /**
   * Returns the entity type name.
   */
  String value();

}
