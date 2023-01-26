package org.example.fuzztest;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import org.example.vulnerabilities.CommandExecution;

public class CommandInjectionFuzzTest {

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        CommandExecution pingExec = new CommandExecution();
        try {
            pingExec.executeShell("ping -c "+data.consumeRemainingAsAsciiString());
        } catch (Exception ignored) {
        }
    }
}
