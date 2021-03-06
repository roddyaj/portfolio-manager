#!/bin/bash

INSTALL=install

set -x

mvn clean
rm -r $INSTALL
mvn package
mkdir -p $INSTALL
mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=$INSTALL
cp target/*.jar $INSTALL
