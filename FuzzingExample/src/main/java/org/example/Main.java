package org.example;

import org.example.vulnerabilities.CommandExecution;
import org.example.vulnerabilities.NullPointerDereference;
import org.example.web.WebInterface;

/**
 * @author Nico Werner Keller
 * @version 1.0
 * @since 15.01.2023
 * Main method which runs the application. Creates an instance of CommandLineInterface and WebInterface
 * to start the Web-Server and the CLI-Interface.
 */
public class Main {
    public static void main(String[] args) {
        WebInterface web = new WebInterface();
        web.startServer();
    }
}