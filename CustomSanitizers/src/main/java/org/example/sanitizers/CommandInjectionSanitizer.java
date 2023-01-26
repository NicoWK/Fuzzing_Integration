package org.example.sanitizers;



import com.code_intelligence.jazzer.api.FuzzerSecurityIssueCritical;
import com.code_intelligence.jazzer.api.HookType;
import com.code_intelligence.jazzer.api.Jazzer;
import com.code_intelligence.jazzer.api.MethodHook;

import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.regex.Pattern;

public class CommandInjectionSanitizer {
    @MethodHook(
            type = HookType.BEFORE,
            targetClassName = "java.lang.ProcessImpl",
            targetMethod = "start",
            additionalClassesToHook = {"java.lang.ProcessBuilder"}
    )
    public static void processImplStartHook(
            MethodHandle method, Object thisObject, Object[] arguments, int hookId) {
        if (arguments.length > 0) {
            String cmd = (String) arguments[0];
            if (Objects.equals(cmd, "inject")) {
                Jazzer.reportFindingFromHook(
                        new FuzzerSecurityIssueCritical("OS Command Injection"));
            }else if (!Pattern.matches("^sh -c '[a-zA-Z0-9\\s]*-?[a-zA-Z0-9\\s]*'$", cmd)) {
                Jazzer.reportFindingFromHook(
                        new FuzzerSecurityIssueCritical("OS Command Injection"));
            } else {
                Jazzer.guideTowardsEquality(cmd, "inject", hookId);
            }
        }
    }
}
