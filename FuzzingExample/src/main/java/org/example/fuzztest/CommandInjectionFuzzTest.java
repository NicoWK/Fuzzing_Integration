package org.example.fuzztest;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import org.example.vulnerabilities.CommandExecution;

/**
 * This class contains the fuzz test, which can be run using a custom sanitizer. The included vulnerability is a command injection.
 */
public class CommandInjectionFuzzTest {

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        CommandExecution pingExec = new CommandExecution();
        try {
            pingExec.executePing(data.consumeRemainingAsAsciiString());
        } catch (Exception ignored) {
        }
    }
}
