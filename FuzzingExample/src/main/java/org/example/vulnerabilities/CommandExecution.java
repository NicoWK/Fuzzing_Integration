package org.example.vulnerabilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import static java.lang.Runtime.getRuntime;

public class CommandExecution {

    //OS-Command Injection possible: Executes a command based on the user-input
    // NullPointer Dereference, the command could be empty

    /**
     * Executes a command passed as a string and returns the output as a string.
     * The command is executed in a subprocess and its result is captured.
     * If the command contains quotes, they are replaced with apostrophes in the output.
     *
     * @param command the command to be executed.
     * @return the output of the executed command as a string.
     * @throws RuntimeException if an IOException or InterruptedException occurs during the execution of the command
     */
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

    /**
     * Executes a ping command to a specified host and returns the output as a string.
     * The command is executed in a subprocess and its result is captured.
     * If the output contains quotes, they are replaced with apostrophes in the returned string.
      * @param hostname the hostname to ping.
     * @return output of the ping command as a string.
     * @throws RuntimeException if an IOException or InterruptedException occurs during the execution of the command.
     */
    public String executePing(String hostname){
        String line;
        BufferedReader bufferedReader;
        String result = "";

        try {
            //execution of possible malicious user input
            Process process = getRuntime().exec(new String[] {"sh","-c", "ping -c 4 "+ hostname});
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