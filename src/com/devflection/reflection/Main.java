package com.devflection.reflection;

import com.devflection.reflection.threads.PluginLoadThread;
import com.devflection.reflection.threads.UserInputThread;

public class Main {

    public static void main(String[] args) {
        // default values for plugin directory and sleep time between user prompts
        String pluginDirectory = "plugins";
        int sleepTime = 30 * 1000;

        // parsing user inputs for plugin directory and sleep time
        if (args.length == 1) {
            pluginDirectory = args[0];
        } else if (args.length > 1) {
            pluginDirectory = args[0];
            sleepTime = Integer.parseInt(args[1]);
        }

        // create an instance of our plugin loader
        PluginLoader pluginLoader = new PluginLoader(pluginDirectory);

        PluginLoadThread pluginLoadThread = new PluginLoadThread(pluginLoader, sleepTime);
        UserInputThread userInputThread = new UserInputThread(pluginLoader);

        pluginLoadThread.start();
        userInputThread.start();
    }
}

/*

public static void main(String[] args) {

        DevflectionPlugin devflectionPlugin = null;
        URLClassLoader urlClassLoader = null;
        try {
            File file = new File("plugins/Plugin1.jar");
            // preapre the classLoader for the jar file
            String jarURL = "jar:" + file.toURI().toURL() + "!/";
            URL[] urls = {new URL(jarURL)};
            urlClassLoader = new URLClassLoader(urls);

            // for each class in the jar file
            for (String className : getAllClassNamesFrom(new JarFile(file))) {
                // we load the class using the URLClassLoader that has the link to the current jar file
                Class<?> aClass = Class.forName(className, false, urlClassLoader);

                // using reflection we check if class implements our plugin interface - DevflectionPlugin
                // and that it is not the interface class
                if (DevflectionPlugin.class.isAssignableFrom(aClass) && aClass != DevflectionPlugin.class) {
                    // using reflection we create an instance of the plugin
                    devflectionPlugin = (DevflectionPlugin) aClass.newInstance();
                    devflectionPlugin.startPlugin();
                }
            }

            if (devflectionPlugin != null) {
                devflectionPlugin.stopPlugin();
            }
        } catch (Exception e) {
            // if we encounter an exception or error return an empty list for this jar and continue
            e.printStackTrace();
        }

        try {
            System.out.println("closing url class loader");
            if (urlClassLoader != null) {
                urlClassLoader.close();
            }
            System.out.println("Done closign...");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i=0; i<5; i++) {
            System.out.println("Doing gc again...");
            System.gc();
        }
        System.out.println("Done with gcs...");

        try {
            System.out.println("GOing to sleep....");
            Thread.sleep(30*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("I'm done....");
    }

    private static List<String> getAllClassNamesFrom(JarFile jarFile) {
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


*/