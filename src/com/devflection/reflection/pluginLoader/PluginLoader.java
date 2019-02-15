package com.devflection.reflection.pluginLoader;

import com.devflection.reflection.DevflectionPlugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader implements IPluginLoader {

    private static final String CLASS_EXTENSION = ".class";
    private static final String JAR_EXTENSION = ".jar";

    private final String pluginDirectoryPath;
    private final Set<DevflectionPluginHolder> plugins;

    public PluginLoader(String pluginDirectoryPath) {
        this.pluginDirectoryPath = pluginDirectoryPath;
        this.plugins = new HashSet<>();
    }

    @Override
    public void loadPlugins() {
        File pluginDirectory = new File(pluginDirectoryPath);
        if (!pluginDirectory.isDirectory()) {
            throw new IllegalArgumentException("Target plugin directory path is not a directory.");
        }

        // get all Jar files from directory
        Arrays.stream(pluginDirectory.listFiles())
                .filter(file -> file.getName().endsWith(JAR_EXTENSION))
                .forEach(this::createClassLoaderAndCreatePluginInstance);
    }

    private void createClassLoaderAndCreatePluginInstance(File file) {
        Optional<URLClassLoader> urlClassLoaderForFile = createURLClassLoaderForFile(file);
        if (urlClassLoaderForFile.isPresent()) {
            loadPluginFromJar(file, urlClassLoaderForFile.get());
        }
    }

    private Optional<URLClassLoader> createURLClassLoaderForFile(File file) {
        try {
            // prepare the classLoader for the jar file
            String jarURL = "jar:" + file.toURI().toURL() + "!/";
            URL[] urls = new URL[]{new URL(jarURL)};
            return Optional.of(new URLClassLoader(urls));
        } catch (MalformedURLException e) {
            // log error
            return Optional.empty();
        }
    }

    private void loadPluginFromJar(File file, URLClassLoader urlClassLoader) {
        try (JarFile jarFile = new JarFile(file)) {
            // for each class in the jar file
            getAllClassNamesFrom(jarFile)
                    .forEach(className -> createDevflectionPluginHolderForClass(className, urlClassLoader));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getAllClassNamesFrom(JarFile jarFile) {
        // iterate over all entries in the jar file and check if they end with .class, then replace '/' chars with '.'
        // and remove the .class extension.
        // The returning format should be a full classname, e.g. com.devflection.reflection.pluginLoader.PluginLoader
        return jarFile.stream()
                .map(JarEntry::getName)
                .filter(name -> name.endsWith(CLASS_EXTENSION))
                .map(name -> name.replace("/", "."))
                .map(name -> name.substring(0, name.length() - CLASS_EXTENSION.length()))
                .collect(Collectors.toList());
    }

    private void createDevflectionPluginHolderForClass(String className, URLClassLoader urlClassLoader) {
        try {
            // we load the class using the URLClassLoader that has the link to the current jar file
            Class<?> aClass = Class.forName(className, true, urlClassLoader);

            // using reflection we check if class implements our plugin interface - DevflectionPlugin
            // and that it is not the interface class
            if (DevflectionPlugin.class.isAssignableFrom(aClass) && aClass != DevflectionPlugin.class) {
                System.out.println(getClass() + ": Found class '" + aClass + "' that implements the plugin interface.");
                // using reflection we create an instance of the plugin
                DevflectionPlugin devflectionPlugin = (DevflectionPlugin) aClass.newInstance();
                DevflectionPluginHolder pluginHolder = new DevflectionPluginHolder(className, devflectionPlugin, urlClassLoader);
                plugins.add(pluginHolder);
            }
        } catch (IllegalAccessException e) {
            // log that we cannot access class
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startPlugins() {
        // go over all the instances and start them if they are not already started
        plugins.stream()
                .filter(holder -> !holder.isRunning())
                .forEach(DevflectionPluginHolder::startPlugin);
    }

    @Override
    public void stopPlugins() {
        // go over all instances and stop them
        // wait a bit for all plugins to stop
        // close the class loaders
        // proc GC so we can delete the jars
        plugins.forEach(DevflectionPluginHolder::stopPlugin);
        plugins.clear();

        forceSystemGarbageCollection();
    }

    private void forceSystemGarbageCollection() {
        for (int i = 0; i < 5; i++) {
            System.gc();
        }
    }

    class DevflectionPluginHolder {

        private DevflectionPlugin pluginInstance;
        private URLClassLoader classLoader;
        private String classname;
        private boolean running = false;

        public DevflectionPluginHolder(String classname, DevflectionPlugin pluginInstance, URLClassLoader classLoader) {
            this.classname = classname;
            this.pluginInstance = pluginInstance;
            this.classLoader = classLoader;
        }

        public void startPlugin() {
            pluginInstance.startPlugin();
            running = true;
        }

        public void stopPlugin() {
            pluginInstance.stopPlugin();
            running = false;

            try {
                Thread.sleep(3 * 1000);

                classLoader.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isRunning() {
            return running;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DevflectionPluginHolder) {
                DevflectionPluginHolder that = (DevflectionPluginHolder) obj;
                return this.classname.equals(that.classname);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return classname.hashCode();
        }
    }
}
