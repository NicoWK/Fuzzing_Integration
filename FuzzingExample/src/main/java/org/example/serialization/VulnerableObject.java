package org.example.serialization;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class VulnerableObject implements Serializable {
    private static final long serialVersionUID = 1;
    private String name;
    private String command;

    public VulnerableObject(String name, String command) {
        super();
        this.name = name;
        this.command = command;
    }

    /**
     * reads an object from the input stream and deserializes it.
     * This method can result in a deserialization vulnerability if untrusted data is deserialized.
     * In this case, the method blindly executes a runtime command, which can be dangerous and potentially harmful to the system.
     *
     * @param stream the input stream to read the object from
     * @throws Exception if an error occurs while reading the object.
     */
    private void readObject(ObjectInputStream stream) throws Exception {
        //deserialize data
        stream.defaultReadObject();
        //blindly run command
        Runtime.getRuntime().exec(command);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
