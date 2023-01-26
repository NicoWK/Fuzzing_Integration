import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.vulnerabilities.ImproperInputValidation;

public class ImproperInputValidationFuzzTest {

    @FuzzTest
    public void diceRollFuzzTest(FuzzedDataProvider data) {
        ImproperInputValidation improperInputValidation = new ImproperInputValidation();
        int rounds = data.consumeInt();
        System.out.println("Rounds: "+rounds);
        improperInputValidation.rollDice(rounds);
    }

}
