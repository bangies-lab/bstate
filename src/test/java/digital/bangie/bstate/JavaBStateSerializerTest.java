package digital.bangie.bstate;

import digital.bangie.bstate.entity.NonSerializableEntity;
import digital.bangie.bstate.entity.Product;
import digital.bangie.bstate.entity.User;
import digital.bangie.bstate.serialization.BStateSerializationException;
import digital.bangie.bstate.serialization.BStateSerializer;
import digital.bangie.bstate.serialization.JavaBStateSerializer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JavaBStateSerializerTest {
    @Test
    void shouldSerializeAndDeserializeValue() {
        BStateSerializer serializer = new JavaBStateSerializer();

        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Bangie")
                .age(36)
                .build();

        byte[] data = serializer.serialize(user);

        User result = serializer.deserialize(data, User.class);

        assertEquals(user.getId(), result.getId());
        assertEquals("Bangie", result.getName());
        assertEquals(36, result.getAge());
    }

    @Test
    void shouldRejectNullValueWhenSerializing() {
        BStateSerializer serializer = new JavaBStateSerializer();

        BStateSerializationException exception = assertThrows(
                BStateSerializationException.class,
                () -> serializer.serialize(null)
        );

        assertEquals("Cannot serialize null value", exception.getMessage());
    }

    @Test
    void shouldRejectNullDataWhenDeserializing() {
        BStateSerializer serializer = new JavaBStateSerializer();

        BStateSerializationException exception = assertThrows(
                BStateSerializationException.class,
                () -> serializer.deserialize(null, User.class)
        );

        assertEquals("Cannot deserialize empty data", exception.getMessage());
    }

    @Test
    void shouldRejectEmptyDataWhenDeserializing() {
        BStateSerializer serializer = new JavaBStateSerializer();

        BStateSerializationException exception = assertThrows(
                BStateSerializationException.class,
                () -> serializer.deserialize(new byte[0], User.class)
        );

        assertEquals("Cannot deserialize empty data", exception.getMessage());
    }

    @Test
    void shouldRejectNullValueTypeWhenDeserializing() {
        BStateSerializer serializer = new JavaBStateSerializer();

        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Bangie")
                .age(36)
                .build();

        byte[] data = serializer.serialize(user);

        BStateSerializationException exception = assertThrows(
                BStateSerializationException.class,
                () -> serializer.deserialize(data, null)
        );

        assertEquals("Value type cannot be null", exception.getMessage());
    }

    @Test
    void shouldRejectWrongValueTypeWhenDeserializing() {
        BStateSerializer serializer = new JavaBStateSerializer();

        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Bangie")
                .age(36)
                .build();

        byte[] data = serializer.serialize(user);

        BStateSerializationException exception = assertThrows(
                BStateSerializationException.class,
                () -> serializer.deserialize(data, Product.class)
        );

        assertTrue(exception.getMessage().contains("Deserialized value is not of expected type"));
    }

    @Test
    void shouldRejectNonSerializableValue() {
        BStateSerializer serializer = new JavaBStateSerializer();

        NonSerializableEntity value = new NonSerializableEntity("Bangie");

        BStateSerializationException exception = assertThrows(
                BStateSerializationException.class,
                () -> serializer.serialize(value)
        );

        assertEquals("Failed to serialize value", exception.getMessage());
    }

}
