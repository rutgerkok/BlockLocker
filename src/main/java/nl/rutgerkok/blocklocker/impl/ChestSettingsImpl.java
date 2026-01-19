package nl.rutgerkok.blocklocker.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import nl.rutgerkok.blocklocker.AttackType;
import nl.rutgerkok.blocklocker.ChestSettings;
import nl.rutgerkok.blocklocker.ProtectableBlocksSettings;
import nl.rutgerkok.blocklocker.ProtectionType;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.Translator.Translation;
import org.bukkit.block.Block;

class ChestSettingsImpl implements ChestSettings {

  private static final ProtectionType[] PROTECTION_TYPES = ProtectionType.values();

  private final Config config;
  private final Translator translator;
  private final List<ProtectableBlocksSettings> extraProtectables = new ArrayList<>();

  ChestSettingsImpl(Translator translator, Config config) {
    this.translator = Objects.requireNonNull(translator, "translator");
    this.config = Objects.requireNonNull(config, "config");
  }

  @Override
  public boolean allowDestroyBy(AttackType attackType) {
    return config.allowDestroyBy(attackType);
  }

  @Override
  public boolean canProtect(Block block) {
    if (config.canProtect(block)) {
      return true;
    }
    if (!this.extraProtectables.isEmpty()) {
      for (ProtectableBlocksSettings extra : this.extraProtectables) {
        if (extra.canProtect(block)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean canProtect(ProtectionType type, Block block) {
    if (config.canProtect(type, block)) {
      return true;
    }
    if (!this.extraProtectables.isEmpty()) {
      for (ProtectableBlocksSettings extra : this.extraProtectables) {
        if (extra.canProtect(type, block)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Optional<Date> getChestExpireDate() {
    int days = config.getAutoExpireDays();
    if (days <= 0) {
      return Optional.empty();
    }

    // Calculate the cutoff date
    Calendar calendar = Calendar.getInstance(Locale.US);
    calendar.add(Calendar.DAY_OF_MONTH, -days);
    Date cutoffDate = calendar.getTime();

    return Optional.of(cutoffDate);
  }

  @Override
  public boolean getConnectContainers() {
    return this.config.getConnectContainers();
  }

  @Override
  public int getDefaultDoorOpenSeconds() {
    return config.getDefaultDoorOpenSeconds();
  }

  @Override
  public List<ProtectableBlocksSettings> getExtraProtectables() {
    return this.extraProtectables;
  }

  @Override
  public String getFancyLocalizedHeader(SignType signType, String header) {
    List<String> headers = translator.getAll(getTranslationKey(signType));

    for (String head : headers) {
      if (head.equalsIgnoreCase(header)) {
        return header;
      }
    }

    return translator.get(getTranslationKey(signType));
  }

  @Override
  public Optional<ProtectionType> getProtectionType(Block block) {
    for (ProtectionType type : PROTECTION_TYPES) {
      if (canProtect(type, block)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }

  @Override
  public List<String> getSimpleLocalizedHeaders(SignType signType) {
    return translator.getAllWithoutColor(getTranslationKey(signType));
  }

  private Translation getTranslationKey(SignType signType) {
    switch (signType) {
      case MORE_USERS:
        return Translation.TAG_MORE_USERS;
      case PRIVATE:
        return Translation.TAG_PRIVATE;
    }
    throw new AssertionError("Unknown type: " + signType);
  }
}
