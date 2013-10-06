package nl.knaw.huygens.timbuctoo.facet;

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

  Class<? extends CustomIndexer> customIndexer() default CustomIndexer.NoopIndexer.class;

  /** Does the user interface contain a facet based on this field? */
  boolean isFaceted() default false;

  boolean isComplex() default false;

  boolean canBeEmpty() default false;

  FacetType facetType() default FacetType.LIST;

  String title() default "";

  boolean isSortable() default false;

}
