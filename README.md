# Timbuctoo

Timbuctoo is a graph-repository for storing and querying research datasets that are interconnected. 
You can have multiple views over something that we as humans would consider to be the same object. 
Such as a person, a country, or even something more abstract such as a time period. 
Each research asserts its own data about the entity.

## Sneak peek

To get a quick look at timbuctoo go to: https://repository.huygens.knaw.nl

## Technologies

Timbuctoo is a Java Jersey/JAX-RS webservice, that uses Tinkerpop to connect to a Neo4J graph database. This is all glued together by the Dropwizard framework.

## Building

To build your own version, make sure you have 'java 8' and 'maven 3' installed and then execute `mvn package` from the root folder of this repository.
To run the version that you built you can use the shell script `timbuctoo-instancev4/run.sh`. For more information about the command line options read the script.
