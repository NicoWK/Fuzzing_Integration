package org.example.vulnerabilities;

import java.io.*;

public class SerializationHelper {
    /**
     * deserializes an object.
     *
     * @param obj the serialized object to be deserialized
     * @return The deserialized object.
     */
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

    /**
     * serializes an object.
     *
     * @param object the object to be serialized
     * @return A byte array representing the serialized object.
     * @throws IOException
     */
    public byte[] serialize(Serializable object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}

