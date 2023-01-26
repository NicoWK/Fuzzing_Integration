import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.vulnerabilities.NullPointerDereference;

public class NullPointerDereferenceFuzzTest {

    @FuzzTest
    public void calculateCostFuzzTest(FuzzedDataProvider data) {
        String a = data.consumeRemainingAsString();
        NullPointerDereference nullPointerDereference = new NullPointerDereference();
        String result = nullPointerDereference.validateNames(a);
    }
}
