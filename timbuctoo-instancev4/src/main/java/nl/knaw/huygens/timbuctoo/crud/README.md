## Context
Timbuctoo offers a REST interface to edit data within different datasets.
This API precedes the RDF data model and even the graph database underpinnings.
Recently we have performed a clean re-implementation of the crud API based on the graph database to clear out many intermediate layers that are no longer needed.

This package contains code that translates a timbuctoo specific JSON format into changes on the Tinkerpop graph.

## Responsibilies
This package is responsible for
 * Parsing various formats (currently excel only)
 * generating "collections" (i.e. tables, sheets, files) containing "rows" of "fields"
 * handling imports of arbitrary size (only limited by the space limitations of the target database)

It is currently not responsible for
 * converting the data to a specific data type (it should output plain text)
 * handling links (i.e. foreign keys or edges)
 * handling data that cannot be expressed as collections of rows

After an import is finished all traces of the original format should be gone.

## Data model
The timbuctoo database is divided into datasets.
Each Virtual Research Environment used to have it's own dataset and each dataset would be solely for a specific VRE.
Therefore a dataset is itself called a VRE.
A VRE has *collections*.
Within a collection there are *entities*.
Each entity belongs to one and only one collection.
The collection that an entity belongs to is also refered to as it's *type*. 
When we say *type* we are also using the singular version of the collection name, when we say *collection* we always uses the plural form.
However, pluralisation is only done by appending an 's' (So a collection of people would have *people* as type and *peoples* as collection name).

Instead of using something like OWL same-as relations timbuctoo handled the open information model by letting the reader specify along which VRE an entity should be viewed.
When viewing along one VRE the properties from that VRE were shown, requesting the same entity (same identifier) using a different VRE would show different properties.
The same identifier would refer to the same vertex, properties were namespaced by their type name.
Types are namespaced by the VRE name, making properties also namespaced by their VRE name.

VRE configuration has only recently been moved to the database, it used to be embedded in Java code.

Outside of the normal VRE's there is a VRE called Admin.

There is also a VRE called Base.



```
```

## Contents of this package
 * [TinkerpopJsonCrudService](TinkerpopJsonCrudService.java) is the entry point. It contains methods for creating, reading, updating and deleting entities and will modify the database according to the above schedule.
  