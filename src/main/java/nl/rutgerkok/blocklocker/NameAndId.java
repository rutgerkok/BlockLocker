package nl.rutgerkok.blocklocker;

import java.util.UUID;

import org.apache.commons.lang.Validate;

/**
 * Represents a pair of name and id. Both the name and id will always be
 * present.
 *
 */
public final class NameAndId {
    /**
     * Creates a new name and id pair.
     *
     * @param name
     *            The name.
     * @param id
     *            The id.
     * @return The pair.
     */
    public static final NameAndId of(String name, UUID id) {
        Validate.notNull(name, "name may not be null");
        Validate.notNull(id, "id may not be null");
        return new NameAndId(name, id);
    }

    private final UUID id;
    private final String name;

    private NameAndId(String name, UUID id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NameAndId other = (NameAndId) obj;
        if (!id.equals(other.id)) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the id.
     *
     * @return The id.
     */
    public UUID getUniqueId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "NameAndId [id=" + id + ", name=" + name + "]";
    }

}
