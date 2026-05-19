package digital.bangie.bstate.entity;

public class NonSerializableEntity {
    private final String name;

    public NonSerializableEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
