package nl.rutgerkok.blocklocker.impl;

import nl.rutgerkok.blocklocker.ChestSettings;
import nl.rutgerkok.blocklocker.SignType;
import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.Translator.Translation;

import org.bukkit.Material;

import com.google.common.base.Optional;

class ChestSettingsImpl implements ChestSettings {

    private final Translator translator;

    ChestSettingsImpl(Translator translator) {
        this.translator = translator;
    }

    @Override
    public boolean canProtect(ProtectionType type, Material material) {
        Optional<ProtectionType> protectionType = getProtectionType(material);
        if (!protectionType.isPresent()) {
            return false;
        }
        return protectionType.get().equals(type);
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
    public String getFancyLocalizedHeader(SignType signType) {
        return translator.get(getTranslationKey(signType));
    }

    @Override
    public Optional<ProtectionType> getProtectionType(Material material) {
        switch (material) {
            case ANVIL:
            case BEACON:
            case BREWING_STAND:
            case BURNING_FURNACE:
            case CHEST:
            case DISPENSER:
            case DROPPER:
            case ENCHANTMENT_TABLE:
            case ENDER_CHEST:
            case FURNACE:
            case TRAPPED_CHEST:
            case TRAP_DOOR:
            case WORKBENCH:
                return Optional.of(ProtectionType.CONTAINER);
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
                return Optional.of(ProtectionType.DOOR);
            default:
                return Optional.absent();
        }
    }

    @Override
    public String getSimpleLocalizedHeader(SignType signType) {
        return translator.getWithoutColor(getTranslationKey(signType));
    }

}
