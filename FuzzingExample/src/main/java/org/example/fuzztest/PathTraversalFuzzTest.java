package org.example.fuzztest;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import org.example.vulnerabilities.FileOperations;
import java.nio.file.InvalidPathException;

/**
 * This class contains the fuzz test, which can be run using a custom sanitizer. The included vulnerability is a path traversal.
 */
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
