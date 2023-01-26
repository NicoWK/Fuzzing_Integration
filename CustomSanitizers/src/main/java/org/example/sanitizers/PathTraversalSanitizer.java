package org.example.sanitizers;

import com.code_intelligence.jazzer.api.FuzzerSecurityIssueHigh;
import com.code_intelligence.jazzer.api.HookType;
import com.code_intelligence.jazzer.api.Jazzer;
import com.code_intelligence.jazzer.api.MethodHook;

import java.lang.invoke.MethodHandle;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTraversalSanitizer {
    private static final String publicFilesRootPath = "safe_dir/";

    @MethodHook(type = HookType.BEFORE, targetClassName = "java.io.File", targetMethod = "<init>",
            targetMethodDescriptor = "(Ljava/lang/String;)V")
    public static void
    fileConstructorHook(MethodHandle handle, Object thisObject, Object[] args, int hookId) {
        String path = (String) args[0];
        Path normalizedPath;
        try {
            normalizedPath = Paths.get(path).normalize();
        } catch (InvalidPathException e) {
            // Invalid paths are correctly rejected by the application.
            return;
        }
        if (!normalizedPath.startsWith(publicFilesRootPath)) {
            // Simply throwing an exception from here would not work as the calling code catches and
            // ignores all Throwables. Instead, use the Jazzer API to report a finding from a hook.
            Jazzer.reportFindingFromHook(new FuzzerSecurityIssueHigh(
                    "Path traversal discovered: '" + path + "' --> '" + normalizedPath + "'"));
        }
    }
}
