package org.example.vulnerabilities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;


public class NullPointerDereference {
    /**
     * This function converts a comma-separated string of names into an ArrayList
     * of strings. If a name is less than 1 character, it will be added as null to the list
     *
     * @param names a string containing comma-separated names
     * @return an ArrayList containing the names as strings, with any names less than 1 character being represented as null
     */
    public static ArrayList<String> convertToArray(String names) {
        ArrayList<String> nameList = new ArrayList<>();
        String currentName = null;
        for (int i = 0; i < names.length(); i++) {
            char currentChar = names.charAt(i);
            if (currentChar != ',') {
                if(currentName == null){
                    currentName = "";
                }
                currentName += currentChar;
            } else {
                nameList.add(currentName);
                currentName=null;
            }
        }
        return nameList;
    }

    /**
     * This function validates a comma-separated string of names, checking if they meet the following requirements:
     * - length must be less than 16 characters
     * - only contains characters A-Z, a-z, 0-9 and _
     *
     * @param names a string containing comma-separated names
     * @return a string indicating if any names do not meet the requirements, or that all names are valid
     */
    public String validateNames(String names) {
        // verify the list of names is not empty
        if (names.isEmpty()) {
            return "String is empty";
        }
        // delete whitespaces for easier separation at the commas
        while (names.contains(" ")){
            names = names.replace(" ", "");
        }
        // could return Null
        ArrayList<String> nameArray = convertToArray(names);
        String invalidNames = "";
        for (String name : nameArray) {
            //Null Pointer Dereference
            if (name.length() >= 32) {
                invalidNames += "Name '" + name + "' is too long, \n";
            }
            if (!name.matches("^[a-zA-Z0-9_]*$")) {
                invalidNames += "Name '" + name + "' contains invalid characters, \n";
            }
        }
        if (!invalidNames.isEmpty()) {
            return "Following names do not meet the requirements: \n" + invalidNames;
        } else {
            return "All names meet the requirements";
        }
    }
}
