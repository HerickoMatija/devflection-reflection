package com.devflection.reflection;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String pluginDirectory = "plugins";
        if (args.length > 0) {
            pluginDirectory = args[0];
        }

        PluginLoader pluginLoader = new PluginLoader(pluginDirectory);
        String userInput = "";

        Scanner scanner = new Scanner(System.in);
        while (!userInput.equals("exit")) {
            pluginLoader.loadJarClasses();

            pluginLoader.startPlugins();

            System.out.println("Press Enter to continue or write 'exit' to stop...");
            userInput = scanner.nextLine();
        }
    }
}
