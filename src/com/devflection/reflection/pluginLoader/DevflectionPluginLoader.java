package com.devflection.reflection.pluginLoader;

import com.devflection.reflection.DevflectionPlugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class DevflectionPluginLoader implements PluginLoader {

    private static final String CLASS_EXTENSION = ".class";
    private static final String JAR_EXTENSION = ".jar";

    private final String pluginDirectoryPath;
    private final Set<DevflectionPluginHolder> plugins;

    public DevflectionPluginLoader(String pluginDirectoryPath) {
        this.pluginDirectoryPath = pluginDirectoryPath;
        this.plugins = new HashSet<>();
    }

    @Override
    public void loadPlugins() {
        File pluginDirectory = new File(pluginDirectoryPath);

        if (!pluginDirectory.isDirectory()) {
            throw new IllegalArgumentException("Target plugin directory path is not a directory.");
        }

        Arrays.stream(pluginDirectory.listFiles())
                .filter(file -> file.getName().endsWith(JAR_EXTENSION))
                .forEach(this::createClassLoaderAndCreatePluginInstance);
    }

    private void createClassLoaderAndCreatePluginInstance(File file) {
        Optional<URLClassLoader> urlClassLoaderForFile = createURLClassLoaderForFile(file);
        urlClassLoaderForFile.ifPresent(urlClassLoader -> loadPluginFromJar(file, urlClassLoader));
    }

    private Optional<URLClassLoader> createURLClassLoaderForFile(File file) {
        try {
            String jarURL = "jar:" + file.toURI().toURL() + "!/";
            URL[] urls = new URL[]{new URL(jarURL)};
            return Optional.of(new URLClassLoader(urls));
        } catch (MalformedURLException e) {
            // log malformed url exception information
            return Optional.empty();
        }
    }

    /**
     * This method opens the given jar file, looks at all the classes and finds the ones implementing the
     * {@link DevflectionPlugin} interface and creates a {@link DevflectionPluginHolder} for each of them and adds
     * them to the set of running plugins
     *
     * @param file The given jar file that we want to check for classes implementing the {@link DevflectionPlugin} interface
     * @param urlClassLoader The class loader that we want to use to load classes in the given jar file
     */
    private void loadPluginFromJar(File file, URLClassLoader urlClassLoader) {
        try (JarFile jarFile = new JarFile(file)) {
            // for each class in the jar file
            getAllClassNamesFrom(jarFile)
                    .stream()
                    .map(className -> createDevflectionPluginHolderForClass(className, urlClassLoader))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(plugins::add);
        } catch (IOException e) {
            // log information about the file not being accessible
        }
    }

    /**
     *
     * This method iterates over all entries in the jar file and checks if they end with .class,
     * then replaces '/' chars with '.' and removes the .class extension.
     * The returning format is a full class name, e.g. com.devflection.reflection.pluginLoader.DevflectionPluginLoader
     *
     * @param jarFile The jar file from which we want to extract all the class names
     * @return A list of strings representing the full class names in this package
     */
    private List<String> getAllClassNamesFrom(JarFile jarFile) {
        return jarFile.stream()
                .map(JarEntry::getName)
                .filter(name -> name.endsWith(CLASS_EXTENSION))
                .map(name -> name.replace("/", "."))
                .map(name -> name.substring(0, name.length() - CLASS_EXTENSION.length()))
                .collect(Collectors.toList());
    }

    /**
     *
     * This method tries to load the class with the given class name and URLClassLoader. Then using reflection
     * it checks if the loaded class implements the {@link DevflectionPlugin} interface. If it does implement the
     * interface, it creates a new {@link DevflectionPluginHolder} and returns it.
     * Otherwise it returns Optional.empty
     *
     * @param className The class name that we want to try to load and check if it
     *                  implements the {@link DevflectionPlugin} interface
     * @param urlClassLoader The class loader with which we want to try loading the given class
     * @return An optional with the created {@link DevflectionPluginHolder} if the class implements the interface
     * or an empty Optional
     */
    private Optional<DevflectionPluginHolder> createDevflectionPluginHolderForClass(String className,
                                                                                    URLClassLoader urlClassLoader) {
        try {
            Class<?> aClass = Class.forName(className, true, urlClassLoader);

            if (DevflectionPlugin.class.isAssignableFrom(aClass) && aClass != DevflectionPlugin.class) {
                System.out.println(getClass() + ": Found class '" + aClass + "' that implements the plugin interface.");
                DevflectionPlugin devflectionPlugin = (DevflectionPlugin) aClass.newInstance();
                return Optional.of(new DevflectionPluginHolder(className, devflectionPlugin, urlClassLoader));
            }
        } catch (IllegalAccessException e) {
            // log that we cannot access class
        } catch (InstantiationException e) {
            // log instantiation exception information
        } catch (ClassNotFoundException e) {
            // log class not found exception information
        }
        return Optional.empty();
    }

    @Override
    public void startPlugins() {
        plugins.forEach(DevflectionPluginHolder::startPlugin);
    }

    @Override
    public void stopPlugins() {
        plugins.forEach(DevflectionPluginHolder::stopPlugin);
        plugins.clear();

        forceSystemGarbageCollection();
    }

    /**
     * This method is a workaround... we want to force the garbage collection to run so there are no more references
     * to specific JAR files, so we can remove them from the plugin directory.
     *
     * This should NOT be done in production code like this.
     */
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

        /**
         * This method starts the plugin instance, if it is not already running.
         */
        public void startPlugin() {
            if (!running) {
                pluginInstance.startPlugin();
                running = true;
            }
        }

        /**
         * This method stops the plugin instance, waits a bit and then also closes the associated URLClassLoader.
         */
        public void stopPlugin() {
            pluginInstance.stopPlugin();
            running = false;

            try {
                Thread.sleep(3 * 1000);

                classLoader.close();
            } catch (InterruptedException e) {
                // log information about interruption
            } catch (IOException e) {
                // log information about IOException
            }
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
