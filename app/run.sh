#!/bin/sh
scriptdir=`dirname $0`
cd "$scriptdir"
java --add-opens=java.desktop/sun.awt=ALL-UNNAMED -jar target/VectorPinball.jar
