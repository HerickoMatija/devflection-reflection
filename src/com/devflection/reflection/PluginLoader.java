package com.devflection.reflection;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader {

    private static final String CLASS_EXTENSION = ".class";
    private static final String JAR_EXTENSION = ".jar";

    private File pluginDirectory;
    private Map<File, List<ClassnameAndPluginInstance>> plugins;
    private Set<ClassnameAndPluginInstance> runningPlugins;

    public PluginLoader(String pluginDirectoryPath) {
        // initialize a file for the plugin directory and throw an exception if it is not a directory
        this.pluginDirectory = new File(pluginDirectoryPath);
        if (!pluginDirectory.isDirectory()) {
            throw new IllegalArgumentException("Target path is not a directory.");
        }
        // initialize the map and set of the plugins
        this.plugins = new HashMap<>();
        this.runningPlugins = new HashSet<>();
    }

    public void startPlugins() {
        loadPluginInstances();
        startPluginInstances();
    }

    public void stopPlugins() {
        loadPluginInstances();
        stopPluginInstances();
    }

    private void loadPluginInstances() {
        // iterate over the files in the plugin directory and for each jar file find and create instances of classes
        // that implement our DevflectionPlugin interface
        Arrays.stream(pluginDirectory.listFiles())
                .filter(file -> file.getName().endsWith(JAR_EXTENSION))
                .forEach(file -> plugins.put(file, getAllPluginsFrom(file)));
    }

    private List<ClassnameAndPluginInstance> getAllPluginsFrom(File file) {
        try {
            // preapre the classLoader for the jar file
            String jarURL = "jar:" + file.toURI().toURL() + "!/";
            URL[] urls = {new URL(jarURL)};
            URLClassLoader urlClassLoader = new URLClassLoader(urls);

            // initialize the list of plugins we find
            List<ClassnameAndPluginInstance> devflectionPlugins = new ArrayList<>();

            // for each class in the jar file
            for (String className : getAllClassNamesFrom(new JarFile(file))) {
                // we load the class using the URLClassLoader that has the link to the current jar file
                Class<?> aClass = Class.forName(className, false, urlClassLoader);

                // using reflection we check if class implements our plugin interface - DevflectionPlugin
                if (DevflectionPlugin.class.isAssignableFrom(aClass)) {
                    // using reflection we create an instance of the plugin
                    DevflectionPlugin devflectionPlugin = (DevflectionPlugin) aClass.newInstance();

                    devflectionPlugins.add(new ClassnameAndPluginInstance(className, devflectionPlugin));
                }
            }

            return devflectionPlugins;

        } catch (Exception | Error e) {
            // if we encounter an exception or error return an empty list for this jar and continue
            return Collections.emptyList();
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

    private void startPluginInstances() {
        // iterate over all of the found plugins and if they are not running yet, start them
        plugins.values().stream()
                .flatMap(Collection::stream)
                .filter(holder -> !runningPlugins.contains(holder))
                .forEach(this::startPluginAndAddToRunningPlugins);
    }

    private void startPluginAndAddToRunningPlugins(ClassnameAndPluginInstance classnameAndPluginInstance) {
        classnameAndPluginInstance.getPluginInstance().startPlugin();
        runningPlugins.add(classnameAndPluginInstance);
    }

    private void stopPluginInstances() {
        // iterate over all of the found plugins and stop them
        plugins.values().stream()
                .flatMap(Collection::stream)
                .forEach(this::stopPluginAndRemoveFromRunningPlugins);
    }

    private void stopPluginAndRemoveFromRunningPlugins(ClassnameAndPluginInstance classnameAndPluginInstance) {
        classnameAndPluginInstance.getPluginInstance().stopPlugin();
        runningPlugins.remove(classnameAndPluginInstance);
    }

    /**
     * A holder class that joins a classname string with the specific
     * {@link DevflectionPlugin} instance we created for it.
     */
    private class ClassnameAndPluginInstance {
        private String className;
        private DevflectionPlugin pluginInstance;

        public ClassnameAndPluginInstance(String className, DevflectionPlugin pluginInstance) {
            this.className = className;
            this.pluginInstance = pluginInstance;
        }

        public DevflectionPlugin getPluginInstance() {
            return pluginInstance;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClassnameAndPluginInstance) {
                ClassnameAndPluginInstance that = (ClassnameAndPluginInstance) obj;
                return this.className.equals(that.className);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return className.hashCode();
        }
    }
}
