package nl.knaw.huygens.timbuctoo.facet;

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
 * Fields are indexed by Solr using a default schema.
 * 
 * Predefined fields are:
 *   <field name="id" type="string" indexed="true" stored="true" required="true"/>
 *   <field name="desc" type="string" indexed="true" stored="true" multiValued="true"/>
 * Dynamic fields use a name pattern:
 *   <dynamicField name="dynamic_sort_*" type="alphaOnlySort" indexed="true" stored="false" multiValued="false"/>
 *   <dynamicField name="dynamic_k_*" type="string" indexed="true" stored="true" multiValued="false"/>
 *   <dynamicField name="dynamic_s_*" type="string" indexed="true" stored="true" multiValued="true"/>
 *   <dynamicField name="dynamic_t_*" type="textgen" indexed="true" stored="true" multiValued="true"/>
 *   <dynamicField name="dynamic_i_*" type="int" indexed="true" stored="true" multiValued="true"/>
 *   <dynamicField name="dynamic_b_*" type="boolean" indexed="true" stored="true" multiValued="true"/>
 *   <dynamicField name="dynamic_d_*" type="date" indexed="true" stored="true" multiValued="true"/>
 */
@Target(value = { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexAnnotation {

  String[] accessors() default {};

  String fieldName() default "";

  /** Does the user interface contain a facet based on this field? */
  boolean isFaceted() default false;

  boolean canBeEmpty() default false;

  FacetType facetType() default FacetType.LIST;

  String title() default "";

  boolean isSortable() default false;

}
