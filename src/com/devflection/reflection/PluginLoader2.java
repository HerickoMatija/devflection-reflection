package com.devflection.reflection;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader2 {

    private static final String CLASS_EXTENSION = ".class";
    private static final String JAR_EXTENSION = ".jar";

    private final String pluginDirectoryPath;
    private final Set<DevflectionPluginHolder> plugins;

    public PluginLoader2(String pluginDirectoryPath) {
        this.pluginDirectoryPath = pluginDirectoryPath;
        this.plugins = new HashSet<>();
    }

    public void loadPlugins() {
        File pluginDirectory = new File(pluginDirectoryPath);
        if (!pluginDirectory.isDirectory()) {
            throw new IllegalArgumentException("Target plugin directory path is not a directory.");
        }

        // get all Jar files from directory
        for(File file : pluginDirectory.listFiles()) {

            if (file.getName().endsWith(JAR_EXTENSION)) {
                try {
                    // preapre the classLoader for the jar file
                    String jarURL = "jar:" + file.toURI().toURL() + "!/";
                    URL[] urls = {new URL(jarURL)};
                    URLClassLoader urlClassLoader = new URLClassLoader(urls);

                    // for each class in the jar file
                    for (String className : getAllClassNamesFrom(new JarFile(file))) {
                        // we load the class using the URLClassLoader that has the link to the current jar file
                        Class<?> aClass = Class.forName(className, true, urlClassLoader);

                        // using reflection we check if class implements our plugin interface - DevflectionPlugin
                        // and that it is not the interface class
                        if (DevflectionPlugin.class.isAssignableFrom(aClass) && aClass != DevflectionPlugin.class) {
                            System.out.println(getClass() + ": Found class '" + aClass + "' that implements the plugin interface.");
                            // using reflection we create an instance of the plugin
                            DevflectionPlugin devflectionPlugin = (DevflectionPlugin) aClass.newInstance();

                            DevflectionPluginHolder pluginHolder = new DevflectionPluginHolder();
                            pluginHolder.classLoader = urlClassLoader;
                            pluginHolder.pluginInstance = devflectionPlugin;
                            pluginHolder.classname = className;

                            plugins.add(pluginHolder);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // if we encounter an exception or error return an empty list for this jar and continue
                    System.out.println(getClass() + ": Encountered a problem while loading " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    private List<String> getAllClassNamesFrom(JarFile jarFile) {
        // iterate over all entries in the jar file and check if they end with .class, then replace '/' chars with '.'
        // and remove the .class extension.
        // The returning format should be a full classname, e.g. com.devflection.reflection.PluginLoader
        return jarFile.stream()
                .map(JarEntry::getName)
                .filter(name -> name.endsWith(CLASS_EXTENSION))
                .map(name -> name.replace("/", "."))
                .map(name -> name.substring(0, name.length() - CLASS_EXTENSION.length()))
                .collect(Collectors.toList());
    }

    public void startPlugins() {
        // go over all the instances and start them if they are not already started
        plugins.stream()
            .filter(holder -> !holder.running)
            .forEach(holder -> {
                holder.pluginInstance.startPlugin();
                holder.running = true;
            });
    }

    public void stopPlugins() {
        // go over all instances and stop them
        // wait a bit for all plugins to stop
        // close the class loaders
        // proc GC so we can delete the jars
    }

    class DevflectionPluginHolder {

        public DevflectionPlugin pluginInstance;
        public URLClassLoader classLoader;
        public String classname;
        public boolean running = false;
    }
}
