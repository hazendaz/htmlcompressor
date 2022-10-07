====
       Copyright 2009-2022 the original author or authors.

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.
====

HtmlCompressor v.${project.version}
http://code.google.com/p/htmlcompressor/
	
Small, fast and very easy to use Java library that minifies given HTML or XML source by removing extra whitespaces, comments and other unneeded characters without breaking the content structure. As a result pages become smaller in size and load faster. A command-line version of the compressor is also available. 


PACKAGE CONTENT:
	/bin - contains main ${project.artifactId}-${project.version}.jar binary, 
			as well as several extra jars (could be useful for IDE integration)
	/doc - javadocs
	/src - sources
	/lib - dependencies (for using with a command line compressor or non-Maven projects)
	pom.xml - Maven POM file

USAGE:
	For java projects add ${project.artifactId}-${project.version}.jar library to your project's classpath
	For a command line usage run:
		java -jar ${project.artifactId}-${project.version}.jar -h
	to get a brief description of available parameters.
	
	Please refer to http://code.google.com/p/htmlcompressor/ for the detailed documentation.

PROJECT BUILD:
	- Install JDK v.11+ (https://www.oracle.com/java/technologies/downloads/)
	- Install Maven v.3.8.6+ (http://maven.apache.org/download.html)
	- Run build.bat or build.sh
	- Compiled binaries will be placed in /target subdirectory

CHANGELOG:
	http://code.google.com/p/htmlcompressor/wiki/Changelog
