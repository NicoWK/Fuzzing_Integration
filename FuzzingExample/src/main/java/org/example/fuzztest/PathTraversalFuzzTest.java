package org.example.fuzztest;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import org.example.vulnerabilities.FileOperations;
import org.junit.Assert;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTraversalFuzzTest {

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        FileOperations fileOperations = new FileOperations();
        String filename = data.consumeRemainingAsAsciiString();
        try {
            String filePath  = fileOperations.createFile("safe_dir", filename);
            fileOperations.writeToFile(filePath, "File content");
        } catch (NullPointerException | InvalidPathException  ignored){

        }
    }
}
