# Devflection reflection example

## Introduction
This project shows an example of how reflection in Java might be used to 
facilitate dynamic plugin loading.

## How to build and run

cd into the base directory (DevflectionReflection)
 
create a manifest file
 
execute
	javac src/com/devflection/reflection/Main.java -sourcepath src -d target

	jar -cmfv MANIFEST.mf DevflectionReflection.jar -C target .
	
	java -jar DevflectionReflection.jar	

