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

    private String pluginDirectoryPath;
    private Map<File, List<DevflectionPlugin>> plugins;
    private Set<DevflectionPlugin> runningPlugins;

    public PluginLoader(String pluginDirectory) {
        this.pluginDirectoryPath = pluginDirectory;
        this.plugins = new HashMap<>();
        this.runningPlugins = new HashSet<>();
    }

    public void loadJarClasses() {
        File pluginDirectory = new File(pluginDirectoryPath);

        if (!pluginDirectory.isDirectory()) {
            throw new IllegalArgumentException("Target path is not a directory.");
        }

        for (File file : pluginDirectory.listFiles()) {
            if (file.getName().endsWith(JAR_SUFFIX)) {
                plugins.put(file, getAllPluginsFromJar(file));
            }
        }
    }

    private List<DevflectionPlugin> getAllPluginsFromJar(File file) {
        List<DevflectionPlugin> devflectionPlugins = new ArrayList<>();
        try {
            String jarURL = "jar:" + file.toURI().toURL() + "!/";
            URL urls[] = {new URL(jarURL)};
            URLClassLoader ucl = new URLClassLoader(urls);

            for (String className : getAllClassesFromFile(new JarFile(file))) {
                Class<?> aClass = Class.forName(className, false, ucl);

                if (DevflectionPlugin.class.isAssignableFrom(aClass)) {
                    DevflectionPlugin devflectionPlugin = (DevflectionPlugin) aClass.newInstance();
                    devflectionPlugins.add(devflectionPlugin);
                }
            }
        } catch (Exception | Error e) {
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

    public void startPlugins() {
        plugins.entrySet().stream()
                .filter(this::pluginFileExists)
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(plugin -> !runningPlugins.contains(plugin))
                .forEach(plugin -> {
                    plugin.startPlugin();
                    runningPlugins.add(plugin);
                });
    }

    private boolean pluginFileExists(Map.Entry entry) {
        if (entry.getKey() instanceof File) {
            return ((File) entry.getKey()).exists();
        }
        return false;
    }
}
