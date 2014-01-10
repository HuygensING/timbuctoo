Timbuctoo
=========

## What is Timbuctoo?
Timbuctoo is developed by Huygens ING. A part of it's activities is research based around historical events. 
Timbuctoo solves two problems. The first one is to provide an infrastructure where multiple research projects can store their data. The second is that it allows multiple projects to have different opinions about the stored data. So overlapping data can easily be shared between multiple projects.

## How does it work?
Timbuctoo is a Java REST-service, that uses the JAX-RS implementation Jersey. It uses a Mongo database to store the data. It uses Solr for faster search results. It supports real persistency by using a [Handle service](http://handle.net/). The whole system is tested using Apache Tomcat.

## This guide
This guide is still a work in progress. Now it should contain the bare minimum to get a software developer started with the application.

## Getting Started

### Pre conditions
This guide expects that Mongo and a web server (i.e. Apache Tomcat) are installed. For compilation you need a Java 6 compiler and a Maven installed, that you can use in your command line.

#### Example configuration
You have to change one thing in the example configuration (`timbuctoo-war-config-example`) in order to make your web application work. That is the database section of `config.xml`:

```xml
<database>
  <!-- Mongo database -->
  <host>{server}</host>
  <name>{db name}</name> <!-- Could be any name if none is created -->
  <port>{port}</port> <!-- default mongo port is 27017 -->
  <user>{user name}</user>
  <password>{password}</password>
</database>
```
    
### Compilation
Go to the root of your git repository and execute the `mvn clean package -P example`. This will generate a war-file in the `timbuctoo-rest/target` Rename this file to `timbuctoo.war` and deploy it on your favourite web server like Apache Tomcat 6 or higher.

### Demo pages

Timbuctoo has a few pages made for demo purposes. The pages are found in the `webapp/static` folder in the `timbuctoo-rest` Maven module. `index.html` shows the content of the database and the different solr indexes. `search.html` demonstrates a simple text  search page. Each `VRE` can search its own objects. Like the `VRE` *DWC* can search on each type defined in the Java package `nl.knaw.huygens.timbuctoo.model.dwcbia`. The same is true for *DutchCarribean*. The *Base* VRE will can search in the `model` root package. These model packages can be found in the `timbuctoo-core` Maven module.

Another interesting page is `user_info.html` in the `example_vre` subfolder  this demonstrates how to request a secured resource. This page can be reached on `{base_url}/timbuctoo/static/example_vre/user_info.html

### Supported REST calls
The demo pages show a small amount of REST resources availlable in Timbuctoo. Each non abstract class in the in model packages has it's own resource. The next example will show the available REST calls of the resource for the model class `nl.knaw.huygens.timbuctoo.model.dcar.DCARArchive`. 


* `{base_url}/timbuctoo/domain/dcararchives/ [GET]` shows the first 200 items of the collections
* `{base_url}/timbuctoo/domain/dcararchives/ [POST]` creates a new archive
* `{base_url}/timbuctoo/domain/dcararchives/ARCH000000001838 [GET]` shows a single item
* `{base_url}/timbuctoo/domain/dcararchives/ARCH000000001838 [PUT]` updates a single item
* `{base_url}/timbuctoo/domain/dcararchives/ARCH000000001838 [DELETE]` removes a single item

The resource name of a model can be determined as the lowercase name of the class postfixed with an s.

### Creating test data
In the `nl.knaw.huygens.timbuctoo.tools.util` package of the `timbuctoo-tools` module the `PrototypeCreator` gives an overview of how Timbuctoo expects the json be formatted. 
