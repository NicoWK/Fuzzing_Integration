import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.database.Database;
import org.example.model.User;
import org.junit.Assert;
import org.junit.Test;

public class SQLInjectionuzzTest {

    private Database db;

    private Database initializeDatabase(){
        Database db = new Database();
        db.connect();
        return db;
    }

    private String fuzzedString(FuzzedDataProvider data){
        return data.consumeString(10);
    }

    @Test // a unit test to verify the functionality of inserting data
    public void insertDataUnitTest(){ 
        db = initializeDatabase();
        User testuser = new User("testUser", "testPassword");
        Boolean result = db.create(testuser); // execute vulnerable function without errors
        Assert.assertTrue(result); // assert that the provided test case returns true
    }

    @FuzzTest // the fuzzer generates inputs in data based on a unit's run-time behaviour
    public void insertDataFuzzTest(FuzzedDataProvider data) {
        db = initializeDatabase();
        try {
            User testUser = new User(data.consumeString(55), data.consumeString(55)); // use generated data for object creation
            db.create(testUser); // execute vulnerable function and detect an SQL injection + RCE
        } catch (Exception e){
        }
    }
}
