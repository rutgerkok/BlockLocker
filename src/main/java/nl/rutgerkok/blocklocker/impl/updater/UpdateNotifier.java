package nl.rutgerkok.blocklocker.impl.updater;

import java.net.URL;
import java.util.Optional;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import nl.rutgerkok.blocklocker.Permissions;
import nl.rutgerkok.blocklocker.Translator;
import nl.rutgerkok.blocklocker.Translator.Translation;

/**
 * Used to notify people that an update of the plugin is out.
 *
 * <p>
 * When registered as a listener, the notification will be send to all admins
 * that are logging in.
 */
final class UpdateNotifier implements Listener {

    private final UpdateResult result;
    private final Translator translator;

    public UpdateNotifier(Translator translator, UpdateResult result) {
        this.result = Preconditions.checkNotNull(result);
        this.translator = Preconditions.checkNotNull(translator);

        Preconditions.checkArgument(result.hasNotification(), "result must have a notification");

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(Permissions.CAN_BYPASS)) {
            sendNotification(player);
        }
    }

    /**
     * Sends a notification to the given person.
     * 
     * @param sender
     *            The person to send to.
     */
    void sendNotification(CommandSender sender) {
        UpdateCheckResult checkResult = result.getUpdateCheckResult();
        String newVersion = checkResult.getLatestVersion().orElse("?");

        // Show status
        switch (result.getStatus()) {
            case AUTOMATICALLY_UPDATED:
                translator.sendMessage(sender, Translation.UPDATER_UPDATED_AUTOMATICALLY, newVersion);
                break;
            case MANUAL_UPDATE:
                translator.sendMessage(sender, Translation.UPDATER_UPDATE_AVAILABLE, newVersion);
                break;
            case UNSUPPORTED_SERVER:
                Set<String> mcVersions = result.getUpdateCheckResult().getMinecraftVersions();
                String mcVersionsString = Joiner.on(", ").join(mcVersions);
                translator.sendMessage(sender, Translation.UPDATER_UNSUPPORTED_SERVER, newVersion, mcVersionsString);
                break;
            default:
                throw new AssertionError("Umhandled case: " + result.getStatus());
        }

        // More information
        Optional<URL> infoUrl = checkResult.getInfoUrl();
        if (infoUrl.isPresent()) {
            translator.sendMessage(sender, Translation.UPDATER_MORE_INFORMATION, infoUrl.get().toString());
        }
    }

}
