package com.devflection.reflection;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String pluginDirectory = "plugins";
        if (args.length > 0) {
            pluginDirectory = args[0];
        }

        PluginLoader pluginLoader = new PluginLoader(pluginDirectory);
        Scanner scanner = new Scanner(System.in);

        String userInput = "";
        while (!userInput.equals("exit")) {
            pluginLoader.loadAndStartPlugins();

            try {
                Thread.sleep(15 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Press Enter to continue or write 'exit' to stop...");
            userInput = scanner.nextLine();
        }
    }
}
