package nl.rutgerkok.chestsignprotect.impl;

import nl.rutgerkok.chestsignprotect.ChestSettings;

import org.bukkit.Material;

import com.google.common.base.Optional;

public class ChestSettingsImpl implements ChestSettings {

    private static final String MORE_USERS = "[More Users]";
    private static final String PRIVATE = "[Private]";

    @Override
    public boolean canProtect(ProtectionType type, Material material) {
        Optional<ProtectionType> protectionType = getProtectionType(material);
        if (!protectionType.isPresent()) {
            return false;
        }
        return protectionType.get().equals(type);
    }

    @Override
    public String getHeader(SignType signType) {
        switch (signType) {
            case MORE_USERS:
                return MORE_USERS;
            case PRIVATE:
                return PRIVATE;
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
