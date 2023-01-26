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
