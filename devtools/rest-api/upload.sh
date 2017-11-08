#!/usr/bin/env bash

echo "Uploading $1 to $DATASETID";
http --form POST ":8080/v5/${USERID:-u33707283d426f900d4d33707283d426f900d4d0d}/${DATASETID}/upload/rdf?forceCreation=true" authorization:"${AUTHTOKEN:-DUMMY}" encoding=UTF-8 fileMimeTypeOverride=application/ld+json file@"$1"
