# DataAccess
> Wrapper around data access methods

## Context
We are using a database, but we're not convinced that it's the best one.
We would like to be able to switch to other databases and the *evaluate* whether other databases make more sense.

Furthermore TinkerPop requires us to handle transactions thoughtfully but provides a rather lax API.
We'd like to access the database only in a correct manner.

## Goal
DataAccess will solve both problems by becoming the only way to access the database and by guaranteeing the following attributes.

 * The methods are only accessible while inside an AutoCloseable wrapper that manages the transaction. 
   (So that the client code is forced to think about transaction lifetime)
 * ~~no two transaction may run at the same time~~ (will be possible once no code uses graphWrapper directly anymore)
 * Each method returns an Immutable Value object without TinkerPop or Neo4j references. 
   (So that Timbuctoo is less dependent on one database implementation)
 * A value object will never hit the database to lazy load more data.
   (so that the client code can more easily reason about performance. Only db.something() calls will hit the database)
 * A method is quite tightly coupled to the client. While a method may be used by more then one client is has a very tightly defined implementation and re-use is not expected to be the rule. 
   (So that removing some client code will also clearly remove their constraints on the data model)
    * of course, internally methods may re-use each other freely (so that we do not have code duplication)
 * A mutation is a separate method adhering to the same practices as a retrieval method. 
   (a second requirement to make only db.something() calls hit the database)
 * You might have a mutation method that also retrieves data, but this is not the norm.
   (explicitly mentioned for people who expect full Command Query Separation) 
 * The dataAccess methods will only get or mutate state. They will not trigger or calculate state (i.e. they will not generate handle id's etc, they might require a parameter containing a handle id though)
   (This prevents scope creep because everything could be in dataAccess. It also makes the dataAccess methods easier to test)
     * But: The dataAccess class will perform authorization.
       (This prevents code duplication, and prevents security bugs)
     * But: The dataAccess class will generate the unique ID's
       (because otherwise it would still need to verify uniqueness and throw exceptions which would make the code needlessly complex)
 * ~~You may add a custom DataAccess implementation which only implements a few methods that will then be run in an experiment. So you can try out a new database without having to write a full implementation~~ might be added once we have one full implementation. 
 * initially there is no interface and only one implementation to facilitate easier refactoring
 
## Issues
 * DataAccess
    * replaceRelation
        * throw an AlreadyUpdatedException when the rev of the client is not the latest
            * this is to prevent update wars.
        * throw a distinct Exception when the client tries to save a relation with different source, target or type.
            * we want to be sure the client updates the right relation.
            * throw a new custom exception.
 
## Data model
### Entity
Contains the information of the Entity saved in the database. 
For example a person or a document.
Each Entity has properties.
Most Entities have relations.

```java
public interface Entity {
  List<TimProperty> getProperties();
  
  List<RelationRef> getRelations();
  //...
} 
```

### TimProperty
Contains the information of the property of an Entity.
The TimProperty interface has implementations for all property types used in Timbuctoo.
A TimProperty can be a wrapper around a Java type like String, int, boolean or a Timbuctoo custom type like PersonNamesValue and Datable. 
This way we can provide an implementation (e.g a [DataAccessMethods](./DataAccess.java) implementation, a jersey serializer, or a js client of the API) with a finite list of types that they should be able to handle.

#### Example
If this is the interface
```java
public abstract class TimProperty {
  private final String name;
  private final Value value;

  public TimProperty(String name, Value value){
     this.name = name;
     this.value = value;
   }
 
   public abstract <Type> Tuple<String, Type> convert(PropertyConverter<Type> propertyConverter) throws IOException;
 
   public String getName() {
     return name;
   }
 
   public Value getValue() {
     return value;
   }
}
```
Then we can implement it for two property types like so:

```java
public class PersonNamesProperty extends TimProperty<PersonNames> {
  public PersonNamesProperty(String name, PersonNames value) {
    super(name, value);
  }

  @Override
  public <Type> Tuple<String, Type> convert(PropertyConverter<Type> propertyConverter) throws IOException {
    return propertyConverter.to(this);
  }
}

public class StringProperty extends TimProperty<String> {
  public StringProperty(String name, String value) {
    super(name, value);
  }

  @Override
  public <Type> Tuple<String, Type> convert(PropertyConverter<Type> propertyConverter) throws IOException {
    return propertyConverter.to(this);
  }
}
```

### PropertyConverter
A PropertyConverter converts a Timbuctoo specific type to an output type.
It will also convert an output type to a timbuctoo specific type (and thus function like a factory method).
The input of PropertyConverter#from is a the property's name and it's value, so you can choose which converter to use based on the property name.
The from method is responsible for determining what the input value contains and picking the right TimProperty implementation to return.

The to methods all return the same result type because we don't think adding a type parameter for each result value is needed.
They return a Tuple of String, Type where the left contains the propertyName as defined by the converter. 
This allows a converter to change the name as well.
This is currently needed for our database (which prefixes all properties with the collection and dataset name)
```java
public abstract class PropertyConverter<Type>{
  public TimProperty from(String name, Type value){
    //...
  }
  
  protected abstract PersonNamesProperty createPersonNamesProperty(String name, Type value);
  
  protected abstract StringProperty createStringProperty(String name, Type value);
  
  protected abstract Tuple<String, Type> to(PersonNamesProperty property);
    
  protected abstract Tuple<String, Type> to(StringProperty property);
}
```
 
#### Example

```java
public class JsonPropertyConverter implements TimPropertyConverter<JsonNode> {
  public TinkerPopPropertyConverter(Collection collection) {
    //store the collection for later use in deciding what property type 
  }
  
  protected PersonNamesProperty createPersonNamesProperty(String name, JsonNode value) {
    //...
  }
    
  protected StringProperty createStringProperty(String name, JsonNode value) {
    //...
  }
    
  protected Tuple<String, JsonNode> to(PersonNamesProperty property){
    //...
  }
  
  protected Tuple<String, JsonNode> to(StringProperty property){
    //...
  }
}
```

```java
public class TinkerPopPropertyConverter implements TimPropertyConverter<Object> {
  public TinkerPopPropertyConverter(Collection collection) {
    
  }

  protected PersonNamesProperty createPersonNamesProperty(String name, Object value) {
    //...
  }
    
  protected StringProperty createStringProperty(String name, Object value) {
    //...
  }    

  protected Tuple<String, Object> to(PersonNamesProperty property){
    //...
  }
  
  protected Tuple<String, Object> to(StringProperty propertyName){
    //...
  }
}
```

### RelationRef
Represents a reference of another Entity. 
It contains information like the name of the relation and the display name of the other Entity. 
An Entity does not contain teh related entities.
Only these RelationRefs.

### EntityRelation
Is a way to represent a relation between two Entities. 
This representation is used when a relation is saved in the database.
 

# TransactionFilter
> closes transactions at the end of a request

This is a stopgap method that should prevent the problems that will be really fixed by the DataAccess class.

 1. Dropwizard re-uses threads between requests.
 2. neo4j needs a transaction for read actions
 3. tinkerpop automatically creates a thread local transaction when 
    needed. (also for read actions)
 4. we did not know so we don't close read transactions at the end of 
    a read action

Together this results in a thread that will not see the changes of 
another thread. If it had a transaction open since before the other 
thread had a transaction open. 

I do not completely understand it though, because an open transaction
should be able to see committed data from another transaction (and I 
have verified that the data was indeed committed). However this filter 
does fix the bug.
