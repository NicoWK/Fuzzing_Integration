import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.vulnerabilities.IntegerWraparound;
import org.junit.Assert;


public class IntegerWraparoundFuzzTest {

    private IntegerWraparound initialize(){
        IntegerWraparound integerWraparound = new IntegerWraparound();
        return integerWraparound;
    }

    @FuzzTest
    public void exponentationFuzzTest(FuzzedDataProvider data) {
        IntegerWraparound integerWraparound = initialize();
        int initialCapital = data.consumeInt();
        int monthlySavings = data.consumeInt();
        int investmentPeriod = data.consumeInt();
        int annualInterestRate = data.consumeInt();
        int[] result = integerWraparound.calculateFinalCapital(initialCapital, monthlySavings, investmentPeriod, annualInterestRate);
        // if the final capital is negative, an Integer Wraparound occurred
        Assert.assertTrue(result[2]>=0);
    }


}


