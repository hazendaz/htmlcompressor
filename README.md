# HtmlCompressor #

[![Java CI](https://github.com/hazendaz/htmlcompressor/actions/workflows/ci.yaml/badge.svg)](https://github.com/hazendaz/htmlcompressor/actions/workflows/ci.yaml)
[![Coverage Status](https://coveralls.io/repos/github/hazendaz/htmlcompressor/badge.svg?branch=master)](https://coveralls.io/github/hazendaz/htmlcompressor?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.hazendaz/htmlcompressor.svg)](https://central.sonatype.com/artifact/com.github.hazendaz/htmlcompressor)
[![Apache 2](http://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

![hazendaz](src/site/resources/images/hazendaz-banner.jpg)

See site page [here](https://hazendaz.github.io/htmlcompressor/)

Small, fast and very easy to use Java library that minifies given HTML or XML source by removing extra whitespaces, comments and other unneeded characters without breaking the content structure. As a result pages become smaller in size and load faster. A command-line version of the compressor is also available. 

## PACKAGE CONTENT ##
- ```/bin``` contains main ${project.artifactId}-${project.version}.jar binary, as well as several extra jars (could be useful for IDE integration)
- ```/doc``` javadocs
- ```/src``` sources
- ```/lib``` dependencies (for using with a command line compressor or non-Maven projects)
- ```pom.xml``` Maven POM file

## USAGE ##
- For java projects add ${project.artifactId}-${project.version}.jar library to your project's classpath
- For a command line usage run: ```java -jar ${project.artifactId}-${project.version}.jar -h``` to get a brief description of available parameters.

Please refer to http://code.google.com/p/htmlcompressor/ for the detailed documentation.

## PROJECT BUILD ##
- Install JDK 11+ (https://www.oracle.com/java/technologies/downloads/)
- Install Maven 3.9.1+ (http://maven.apache.org/download.html)
- Run build.bat or build.sh
- Compiled binaries will be placed in /target subdirectory

## CHANGELOG ##
- Changelog for 1.5.3 and before found [here](CHANGELOG.md)

## Looking to help ##
- A lot of docuemntation is still at google code, a good getting started pull request would be to work on translate that data over.  The original export only exported the issues and source code.  Thus far the TODO wiki is 100% copied over, The change log is copied over too but missing links.  Need all documentation copied over.
