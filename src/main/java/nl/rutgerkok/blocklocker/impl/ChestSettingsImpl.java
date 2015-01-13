package nl.rutgerkok.blocklocker.impl;

import nl.rutgerkok.blocklocker.ChestSettings;
import nl.rutgerkok.blocklocker.ProtectionType;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.Translator.Translation;

import org.bukkit.Material;

import com.google.common.base.Optional;

class ChestSettingsImpl implements ChestSettings {

    private static final ProtectionType[] PROTECTION_TYPES = ProtectionType.values();

    private final Config config;
    private final Translator translator;

    ChestSettingsImpl(Translator translator, Config config) {
        this.translator = translator;
        this.config = config;
    }

    @Override
    public boolean canProtect(ProtectionType type, Material material) {
        return config.canProtect(type, material);
    }

    @Override
    public int getDefaultDoorOpenSeconds() {
        return config.getDefaultDoorOpenSeconds();
    }

    @Override
    public String getFancyLocalizedHeader(SignType signType) {
        return translator.get(getTranslationKey(signType));
    }

    @Override
    public Optional<ProtectionType> getProtectionType(Material material) {
        for (ProtectionType type : PROTECTION_TYPES) {
            if (config.canProtect(type, material)) {
                return Optional.of(type);
            }
        }
        return Optional.absent();
    }

    @Override
    public String getSimpleLocalizedHeader(SignType signType) {
        return translator.getWithoutColor(getTranslationKey(signType));
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

    @Override
    public boolean canProtect(Material material) {
        return config.canProtect(material);
    }

}
