package org.example.vulnerabilities;

import java.io.*;

public class SerializationHelper {
    public Object deserialize(byte[] obj){
        Object o = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(obj));
            o = ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {

        }
        return o;
    }

    public byte[] serialize(Serializable object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}

