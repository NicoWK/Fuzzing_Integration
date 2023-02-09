# Vulnerable Application to test Jazzer
This repository includes a project with two modules.
1. `FuzzingExample`: which contains both the intentionally vulnerable web application and fuzz tests that can be run using `CI Fuzz CLI` or `JUnit5`.
2. `CustomSanitizers`: which contains custom sanitizers / bug detectors to identify a Path Traversal and a Command Injection Vulnerability

## Start the Webserver:


## How to fuzz the application:

### Fuzzing via CI Fuzz CLI

1. Install CI Fuzz CLI as [described in its repository](https://github.com/CodeIntelligenceTesting/cifuzz)
2. Run the command
    ```shell
     cifuzz run <fuzztest>
     ```
3. The following fuzz-tests are available:
    - CommandInjectionFuzzTest
    - DeserializationFuzztest
    - ImproperInputValidationFuzzTest
    - NullDereferenceFuzzTest
    - PathTraversalFuzzTest
    - ResourceConsumptionFuzzTest
    - SQLInjectionFuzzTest
4. The fuzz-test should now be built with Maven and then be executed.

### Fuzzing with JUnit5
1. Set the environment variable `JAZZER_FUZZ` to `1` (for example in your IDE)
   ![Set the environment variable](FuzzingExample/docs/EnvironmentVariable.PNG?raw=true "Set environment variable in IntelliJ IDE")
2. Run the FuzzTest from your IDE with or without coverage
   ![Run Fuzz-Test from IDE](FuzzingExample/docs/RunFuzzTest.PNG?raw=true "Run Fuzz-Test from IntelliJ IDE")

### Fuzzing with custom sanitizers

1. Install the Jazzer standalone as [described in its repository](https://github.com/CodeIntelligenceTesting/jazzer)
2. Install [Maven](https://maven.apache.org/download.cgi)
3. Run the following command to build the JAR`s
    ```shell
    mvn clean install
    ```

4. To run Jazzer with the custom Sanitizers, the resulting JAR's must be defined in Jazzers classpath with the `--cp` option

5. The fuzz target is passed to Jazzer using the `--fuzz_target` parameter and includes the following two test classes:
    - `org.example.fuzztest.CommandInjectionFuzzTest`
    - `org.example.fuzztest.PathTraversalFuzzTest`
6. The custom sanitizers are passed to Jazzer using `--custom_hooks` parameters, the following classes are available for this purpose:
    - `org.example.sanitizers.CommandInjectionFuzzTest`
    - `org.example.sanitizers.PathTraversalFuzzTest`
7. Subsequently, the fuzz test can be executed with the following command:
    ```shell
    jazzer --cp=FuzzingExample/target/FuzzingExample-1.0-SNAPSHOT.jar:CustomSanitizers/target/CustomSanitizers-1.0-SNAPSHOT.jar --custom_hooks=org.example.sanitizers.PathTraversalSanitizer --target_class=org.example.fuzztest.PathTraversalFuzzTest  
    ```
   ```shell
   jazzer --cp=FuzzingExample/target/FuzzingExample-1.0-SNAPSHOT.jar:CustomSanitizers/target/CustomSanitizers-1.0-SNAPSHOT.jar --custom_hooks=org.example.sanitizers.CommandInjectionSanitizer --target_class=org.example.fuzztest.CommandInjectionFuzzTest
    ```