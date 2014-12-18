package nl.rutgerkok.chestsignprotect.impl;

import nl.rutgerkok.chestsignprotect.ChestSettings;
import nl.rutgerkok.chestsignprotect.SignType;
import nl.rutgerkok.chestsignprotect.Translator;
import nl.rutgerkok.chestsignprotect.Translator.Translation;

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

    @Override
    public String getLocalizedHeader(SignType signType) {
        switch (signType) {
            case MORE_USERS:
                return translator.get(Translation.TAG_MORE_USERS);
            case PRIVATE:
                return translator.get(Translation.TAG_PRIVATE);
        }
        throw new AssertionError("Unknown type: " + signType);
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
            default:
                return Optional.absent();
        }
    }

}
