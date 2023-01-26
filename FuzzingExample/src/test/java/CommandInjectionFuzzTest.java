import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.vulnerabilities.CommandExecution;

public class CommandInjectionFuzzTest {

   private CommandExecution initialize(){
       CommandExecution commandExecution = new CommandExecution();
       return commandExecution;
   }

    @FuzzTest
    public void pingFuzzTest(FuzzedDataProvider data) {
        CommandExecution pingExec = initialize();
        try {
            pingExec.execute(data.consumeRemainingAsAsciiString());
        } catch (Exception ignored) {

        }
    }
}
