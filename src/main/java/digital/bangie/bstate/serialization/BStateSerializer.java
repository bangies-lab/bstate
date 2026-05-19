package digital.bangie.bstate.serialization;

public interface BStateSerializer {
    <V> byte[] serialize(V value);
    <V> V deserialize(byte[] data, Class<V> valueType);
}
