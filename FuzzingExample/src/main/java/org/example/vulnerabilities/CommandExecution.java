package org.example.vulnerabilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import static java.lang.Runtime.getRuntime;

public class CommandExecution {

    //OS-Command Injection possible: Executes a command based on the user-input
    // NullPointer Dereference, the command could be empty
    public String execute(String command){
        String line;
        BufferedReader bufferedReader;
        String result = "";
        try {
            //execution of possible malicious user input
            Process process = getRuntime().exec(command);
            System.out.println(process.waitFor());
            //output the result of the command
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(isr);
            while((line = bufferedReader.readLine()) != null){
                if (line.contains("\"")){
                    line = line.replace("\"", "'");
                }
                result = result + line + "\n";
            }
            //terminate Java-subprocess
            if (!process.waitFor(40, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
            }
            bufferedReader.close();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String executeShell(String command){
        String line;
        BufferedReader bufferedReader;
        String result = "";

        try {
            //execution of possible malicious user input
            Process process = getRuntime().exec("sh -c"+command);
            System.out.println(process.waitFor());
            //output the result of the command
            InputStreamReader isr = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(isr);
            while((line = bufferedReader.readLine()) != null){
                if (line.contains("\"")){
                    line = line.replace("\"", "'");
                }
                result = result + line + "\n";
            }
            //terminate Java-subprocess
            if (!process.waitFor(40, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
            }
            bufferedReader.close();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}