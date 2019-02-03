package com.devflection.reflection;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // default values for plugin directory and sleep time between user prompts
        String pluginDirectory = "plugins";
        int sleepTime = 5 * 1000;

        // parsing user inputs for plugin directory and sleep time
        if (args.length == 1) {
            pluginDirectory = args[0];
        } else if (args.length > 1) {
            pluginDirectory = args[0];
            sleepTime = Integer.parseInt(args[1]);
        }

        // create an instance of our plugin loader
        PluginLoader pluginLoader = new PluginLoader(pluginDirectory);

        // scanner reads user input which we save on userInput variable
        Scanner scanner = new Scanner(System.in);
        String userInput;

        // infinite loop
        while (true) {

            // get user input
            System.out.println("Press enter to continue or write 'start', 'stop' or 'exit' to start/stop plugins or exit...");
            userInput = scanner.nextLine();

            // execute the action that the user input
            switch (userInput) {
                case "start":
                    pluginLoader.startPlugins();
                    break;
                case "stop":
                    pluginLoader.stopPlugins();
                    break;
                case "exit":
                    break;
            }

            // wait a while between user prompts
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
