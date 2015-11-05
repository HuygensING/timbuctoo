Timbuctoo
=========

## What is Timbuctoo?
Timbuctoo is developed by Huygens ING.
A part of it's activities is research based around historical events.
Timbuctoo solves two problems.

 1. The first one is to provide an infrastructure where multiple research projects can store their data.
 2. The second is that it allows multiple projects to have different opinions about the stored data.
    So overlapping data can easily be shared between multiple projects.

## How does it work?
Timbuctoo is a Java REST-service, that uses the JAX-RS implementation Jersey.
It uses a neo4j database to store the data.
It uses Solr for faster search results.
It supports real persistency by using a [Handle service](http://handle.net/).

## Getting up and running

### Pre conditions
This guide assumes a working installation of maven and java 8.
We also assume that you have a basic knowledge of those systems.

### Configuration
The example configuration (`timbuctoo-war-config-example`) should be enough to get you going.

**Note**: By default data will be placed in the folders: `~/repository`. If you do not want this, change the contents of `/config/settings/home` in `timbuctoo-war-config-example/src/main/resources/config.xml`.

### Compilation
Execute the `mvn clean package -P example` from the timbuctoo root folder.

### Deployment
Execute `./deploy_locally` from the timbuctoo root folder.

### Loading demo data
If you have access to a data set (we currently do not yet ship one in this repo, sorry) you can load it using `./timbuctoo-tools-instance/target/timbuctoo-tools-instance-3.0.0-SNAPSHOT-assembly/bin/baseImport /path/to/dataset` from the timbuctoo root folder.

### Running
Execute `mvn jetty:run-war` from the timbuctoo root folder.

## Browsing the demo data
Timbuctoo has a few pages made for demo purposes.
The pages are found in the `webapp/static` folder in the `timbuctoo-rest` Maven module.
`index.html` shows the content of the database and the different solr indexes.
`search.html` demonstrates a simple text  search page. Each `VRE` can search its own objects.
Like the `VRE` *CKCC* can search on each type defined in the Java package `nl.knaw.huygens.timbuctoo.model.ckcc`.
The same is true for *DutchCarribean*. The *Base* VRE will can search in the `model` root package.
These model packages can be found in the `timbuctoo-core` Maven module.

Another interesting page is `user_info.html` in the `example_vre` subfolder  this demonstrates how to request a secured resource.
This page can be reached on `{base_url}/timbuctoo/static/example_vre/user_info.html`.

### Supported REST calls
The demo pages show a small amount of REST resources availlable in Timbuctoo.
Each non abstract class in the in model packages has it's own resource.
The next example will show the available REST calls of the resource for the model class `nl.knaw.huygens.timbuctoo.model.dcar.DCARArchive`.

* `{base_url}/timbuctoo/domain/dcararchives/ [GET]` shows the first 200 items of the collections
* `{base_url}/timbuctoo/domain/dcararchives/ [POST]` creates a new archive
* `{base_url}/timbuctoo/domain/dcararchives/ARCH000000001838 [GET]` shows a single item
* `{base_url}/timbuctoo/domain/dcararchives/ARCH000000001838 [PUT]` updates a single item
* `{base_url}/timbuctoo/domain/dcararchives/ARCH000000001838 [DELETE]` removes a single item

To support versioning of the API. The new resources will be prefixed with a version number like:
 
* `{base_url}/v1/timbuctoo/domain/dcararchives/ [GET]` shows the first 200 items of the collections

The resource name of a model can be determined as the lowercase name of the class postfixed with an s.

#### Search api

* {base_url}/timbuctoo/v1/search/ckccpersons [POST] creates a new search request on the CKCCPerson-type.
* {base_url}/timbuctoo/v1/search/QURY000000000159 [GET] returns the search result.

A search request could be as simple as:

```json
{"typeString":"ckccperson","term":"Reigersberch","sortParameters":[]}
```

### Creating test data
In the `nl.knaw.huygens.timbuctoo.tools.util` package of the `timbuctoo-tools` module the `PrototypeCreator` gives an overview of how Timbuctoo expects the json be formatted. 
