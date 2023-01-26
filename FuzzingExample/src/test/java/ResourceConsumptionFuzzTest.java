import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.vulnerabilities.ResourceConsumption;

public class ResourceConsumptionFuzzTest {

    @FuzzTest
    public void recursiveFunctionFuzzTest(FuzzedDataProvider data) {
        ResourceConsumption resourceConsumption = new ResourceConsumption();
        int days = data.consumeInt();
        int costPerDay = data.consumeInt();
        resourceConsumption.calculateCostRekursive(days, costPerDay);
    }
}
