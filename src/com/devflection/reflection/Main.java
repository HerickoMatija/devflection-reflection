package com.devflection.reflection;

import com.devflection.reflection.pluginLoader.IPluginLoader;
import com.devflection.reflection.pluginLoader.PluginLoader;
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
        IPluginLoader pluginLoader = new PluginLoader(pluginDirectory);

        PluginLoadThread pluginLoadThread = new PluginLoadThread(pluginLoader, sleepTime);
        UserInputThread userInputThread = new UserInputThread(pluginLoader);

        pluginLoadThread.start();
        userInputThread.start();
    }
}