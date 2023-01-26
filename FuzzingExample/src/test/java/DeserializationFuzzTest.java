import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.vulnerabilities.SerializationHelper;

public class DeserializationFuzzTest {

    @FuzzTest // the fuzzer generates inputs in data based on a unit's run-time behaviour
    public void deserializationFuzzTest(byte[] data) {

        try {
            SerializationHelper serializationHelper = new SerializationHelper();
            serializationHelper.deserialize(data);
        } catch (RuntimeException ignored) {
            // Ignored RuntimeExceptions thrown by readObject().
        }

    }
}
