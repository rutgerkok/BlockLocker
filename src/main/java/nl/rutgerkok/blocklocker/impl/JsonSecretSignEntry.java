package nl.rutgerkok.blocklocker.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import nl.rutgerkok.blocklocker.SecretSignEntry;

/**
 * Implements a secret sign entry on top of JSON. Useful if you need a stand-alone implementation
 * for testing. Also used for reading legacy data.
 */
public final class JsonSecretSignEntry implements SecretSignEntry {

  private final JsonObject object;

  public JsonSecretSignEntry(JsonObject object) {
    this.object = Objects.requireNonNull(object, "object");
  }

  @Override
  public Optional<Boolean> getBoolean(String key) {
    if (!object.has(key)) {
      return Optional.empty();
    }
    return Optional.of(object.get(key).getAsBoolean());
  }

  @Override
  public OptionalInt getInteger(String key) {
    if (!object.has(key)) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(object.get(key).getAsNumber().intValue());
  }

  @Override
  public Optional<String> getString(String key) {
    if (!object.has(key)) {
      return Optional.empty();
    }
    return Optional.of(object.get(key).getAsString());
  }

  @Override
  public Optional<UUID> getUniqueId(String key) {
    JsonElement uuidObject = object.get(key);

    if (uuidObject == null
        || !uuidObject.isJsonPrimitive()
        || !uuidObject.getAsJsonPrimitive().isString()) {
      return Optional.empty();
    }
    try {
      UUID uuid = UUID.fromString(uuidObject.getAsString());
      return Optional.of(uuid);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  @Override
  public void setBoolean(String key, boolean bool) {
    object.addProperty(key, bool);
  }

  @Override
  public void setInteger(String key, int integer) {
    object.addProperty(key, integer);
  }

  @Override
  public void setString(String key, String value) {
    object.addProperty(key, value);
  }

  @Override
  public void setUniqueId(String key, UUID uuid) {
    object.addProperty(key, uuid.toString());
  }
}
