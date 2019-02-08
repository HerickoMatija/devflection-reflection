# Devflection reflection example

## Introduction
This project shows an example of how reflection in Java might be used to 
facilitate dynamic plugin loading.

## How to build and run the main app

cd into the base directory (DevflectionReflection)
    
    cd DevflectionReflection
  
Create the target folder
    
    mkdir target  
  
Compile classes with

	javac src/com/devflection/reflection/Main.java -sourcepath src -d target
	
Create the jar file 

	jar -cmfv src/resources/MANIFEST.mf DevflectionReflection.jar -C target .

Start the jar file

	java -jar DevflectionReflection.jar	

## How to build the plugins

cd into the base directory (DevflectionReflection)

    cd DevflectionReflection
    
For each of the plugins create the target folder
    
    mkdir pluginImplementations\Plugin1\target
    mkdir pluginImplementations\Plugin2\target
    mkdir pluginImplementations\Plugin3\target
 
For each of the plugins compile classes with

	javac pluginImplementations/Plugin1/src/com/devflection/reflection/Plugin1.java -sourcepath pluginImplementations/Plugin1/src -d pluginImplementations/Plugin1/target
	javac pluginImplementations/Plugin2/src/com/devflection/plugin2/Plugin2.java -sourcepath pluginImplementations/Plugin2/src -d pluginImplementations/Plugin2/target
	javac pluginImplementations/Plugin3/src/com/devflection/plugin3/Plugin3.java -sourcepath pluginImplementations/Plugin3/src -d pluginImplementations/Plugin3/target
	
For each of the plugins create the jar file 

	jar -cfv Plugin1.jar -C pluginImplementations/Plugin1/target plugins
	jar -cfv Plugin2.jar -C pluginImplementations/Plugin2/target plugins
	jar -cfv Plugin3.jar -C pluginImplementations/Plugin3/target plugins