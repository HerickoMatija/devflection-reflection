package com.devflection.reflection;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginLoader {

    private static final String CLASS_SUFFIX = ".class";
    private static final String JAR_SUFFIX = ".jar";

    private File pluginDirectory;
    private Map<File, List<ClassnameAndPluginInstance>> plugins;
    private Set<ClassnameAndPluginInstance> runningPlugins;

    public PluginLoader(String pluginDirectoryPath) {
        this.pluginDirectory = new File(pluginDirectoryPath);
        if (!pluginDirectory.isDirectory()) {
            throw new IllegalArgumentException("Target path is not a directory.");
        }
        this.plugins = new HashMap<>();
        this.runningPlugins = new HashSet<>();
    }

    public void loadAndStartPlugins() {
        loadJarClasses();
        startPlugins();
    }

    private void loadJarClasses() {
        Arrays.stream(pluginDirectory.listFiles())
                .filter(file -> file.getName().endsWith(JAR_SUFFIX))
                .forEach(file -> plugins.put(file, getAllPluginClassesFromJar(file)));
    }

    private List<ClassnameAndPluginInstance> getAllPluginClassesFromJar(File file) {
        List<ClassnameAndPluginInstance> devflectionPlugins = new ArrayList<>();

        try {
            String jarURL = "jar:" + file.toURI().toURL() + "!/";
            URL urls[] = {new URL(jarURL)};
            URLClassLoader ucl = new URLClassLoader(urls);

            for (String className : getAllClassesFromFile(new JarFile(file))) {
                Class<?> aClass = Class.forName(className, true, ucl);

                if (DevflectionPlugin.class.isAssignableFrom(aClass)) {
                    DevflectionPlugin devflectionPlugin = (DevflectionPlugin) aClass.newInstance();
                    devflectionPlugins.add(new ClassnameAndPluginInstance(className, devflectionPlugin));
                }
            }
        } catch (Exception | Error e) {
            // ignoring exceptions and errors so we just skip the jar
        }

        return devflectionPlugins;
    }

    private List<String> getAllClassesFromFile(JarFile jarFile) {
        return jarFile.stream()
                .map(JarEntry::getName)
                .filter(name -> name.endsWith(CLASS_SUFFIX))
                .map(name -> name.replace("/", "."))
                .map(name -> name.substring(0, name.length() - CLASS_SUFFIX.length()))
                .collect(Collectors.toList());
    }

    private void startPlugins() {
        plugins.entrySet().stream()
                .filter(this::pluginFileExists)
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(holder -> !runningPlugins.contains(holder))
                .forEach(this::startPluginAndAddToRunning);
    }

    private boolean pluginFileExists(Map.Entry entry) {
        if (entry.getKey() instanceof File) {
            return ((File) entry.getKey()).exists();
        }
        return false;
    }

    private void startPluginAndAddToRunning(ClassnameAndPluginInstance classnameAndPluginInstance) {
        classnameAndPluginInstance.getPluginInstance().startPlugin();
        runningPlugins.add(classnameAndPluginInstance);
    }

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
