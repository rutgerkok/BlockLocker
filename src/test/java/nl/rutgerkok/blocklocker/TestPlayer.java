package nl.rutgerkok.blocklocker;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.mockito.Mockito;

import com.google.common.base.Charsets;

public class TestPlayer {

    public static Player create() {
        return create("TestPlayer");
    }

    public static Player create(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(name.toLowerCase().getBytes(Charsets.UTF_8));
        return create(name, uuid);
    }

    public static Player create(String name, UUID uuid) {
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getName()).thenReturn(name);
        Mockito.when(player.getUniqueId()).thenReturn(uuid);
        return player;
    }
}