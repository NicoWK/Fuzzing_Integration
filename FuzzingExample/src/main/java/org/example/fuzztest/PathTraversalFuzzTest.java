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
        Path normalized;
        String filePath = "";
        try {
            String filename = data.consumeAsciiString(20);
            String path = fileOperations.writeToFile("safe_dir", filename+".txt");
            normalized = Paths.get(path).normalize();
            filePath = normalized.toString();
            //if Path could contain the "../" pattern following the "safe_dir", after normalization it should not start with safe_dir --> Path Traversal
            Assert.assertTrue(filePath.startsWith("safe_dir"));
        } catch (NullPointerException | InvalidPathException  ignored){

        }
    }
}
