package digital.bangie.bstate.serialization;

import java.io.*;

public class JavaBStateSerializer implements BStateSerializer {

    @Override
    public <V> byte[] serialize(V value) {
        if (value == null) {
            throw new BStateSerializationException("Cannot serialize null value");
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
                objectOutputStream.writeObject(value);
            }

            return byteArrayOutputStream.toByteArray();
        } catch (IOException exception) {
            throw new BStateSerializationException("Failed to serialize value", exception);
        }
    }

    @Override
    public <V> V deserialize(byte[] data, Class<V> valueType) {
        if (data == null || data.length == 0) {
            throw new BStateSerializationException("Cannot deserialize empty data");
        }

        if (valueType == null) {
            throw new BStateSerializationException("Value type cannot be null");
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object value = objectInputStream.readObject();

            if (!valueType.isInstance(value)) {
                throw new BStateSerializationException("Deserialized value is not of expected type " + valueType.getName());
            }

            return valueType.cast(value);
        } catch (IOException | ClassNotFoundException exception) {
            throw new BStateSerializationException("Failed to deserialize value", exception);
        }
    }
}
