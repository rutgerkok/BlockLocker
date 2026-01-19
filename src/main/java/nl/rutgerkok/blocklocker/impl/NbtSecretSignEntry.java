package nl.rutgerkok.blocklocker.impl;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import nl.rutgerkok.blocklocker.SecretSignEntry;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/** Implements a secret sign entry on top of NBT tags (using {@link PersistentDataContainer}. */
public final class NbtSecretSignEntry implements SecretSignEntry {

  private static class SecretSignTagType
      implements PersistentDataType<PersistentDataContainer, NbtSecretSignEntry> {

    @Override
    public NbtSecretSignEntry fromPrimitive(
        PersistentDataContainer primitive, PersistentDataAdapterContext context) {
      return new NbtSecretSignEntry(primitive);
    }

    @Override
    public Class<NbtSecretSignEntry> getComplexType() {
      return NbtSecretSignEntry.class;
    }

    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {
      return PersistentDataContainer.class;
    }

    @Override
    public PersistentDataContainer toPrimitive(
        NbtSecretSignEntry complex, PersistentDataAdapterContext context) {
      return complex.data;
    }
  }

  public static final PersistentDataType<PersistentDataContainer, NbtSecretSignEntry> TAG_TYPE =
      new SecretSignTagType();

  private static final WeakReference<Plugin> PLUGIN =
      new WeakReference<>(JavaPlugin.getProvidingPlugin(NbtSecretSignEntry.class));

  static NamespacedKey key(String name) {
    return new NamespacedKey(PLUGIN.get(), name);
  }

  private final PersistentDataContainer data;

  public NbtSecretSignEntry(PersistentDataContainer data) {
    this.data = Objects.requireNonNull(data);
  }

  @Override
  public Optional<Boolean> getBoolean(String key) {
    Byte result = data.get(key(key), PersistentDataType.BYTE);
    if (result == null) {
      return Optional.empty();
    }
    if (result == 0) {
      return Optional.of(Boolean.FALSE);
    }
    return Optional.of(Boolean.TRUE);
  }

  @Override
  public OptionalInt getInteger(String key) {
    Integer integer = data.get(key(key), PersistentDataType.INTEGER);
    if (integer == null) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(integer.intValue());
  }

  @Override
  public Optional<String> getString(String key) {
    return Optional.ofNullable(data.get(key(key), PersistentDataType.STRING));
  }

  @Override
  public Optional<UUID> getUniqueId(String key) {
    long[] array = data.get(key(key), PersistentDataType.LONG_ARRAY);
    if (array == null || array.length != 2) {
      return Optional.empty();
    }
    return Optional.of(new UUID(array[0], array[1]));
  }

  @Override
  public void setBoolean(String key, boolean value) {
    data.set(key(key), PersistentDataType.BYTE, value ? (byte) 1 : (byte) 0);
  }

  @Override
  public void setInteger(String key, int integer) {
    data.set(key(key), PersistentDataType.INTEGER, integer);
  }

  @Override
  public void setString(String key, String value) {
    data.set(key(key), PersistentDataType.STRING, value);
  }

  @Override
  public void setUniqueId(String key, UUID uuid) {
    long[] array = new long[] {uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()};
    data.set(key(key), PersistentDataType.LONG_ARRAY, array);
  }
}
