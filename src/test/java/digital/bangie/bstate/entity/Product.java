package digital.bangie.bstate.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class Product implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final String name;

    private Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static class Builder {
        private UUID id;
        private String name;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
