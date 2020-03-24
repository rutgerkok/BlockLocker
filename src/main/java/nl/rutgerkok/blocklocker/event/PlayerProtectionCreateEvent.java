package nl.rutgerkok.blocklocker.event;

import java.util.Objects;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called just before a protection is created by a player. If you cancel the
 * event, the sign will not be placed, or will be popped off.
 *
 * <p>
 * This event is only fired if a player edits a sign to become a [Private] sign,
 * or if a player right-clicks a protectable block with a sign in their hand,
 * creating a protection. Note that people with world editing tools can bypass
 * this event.
 */
public class PlayerProtectionCreateEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private boolean isCancelled = false;
	private final Block signBlock;

	public PlayerProtectionCreateEvent(Player player, Block signBlock) {
		super(Objects.requireNonNull(player, "player"));
		this.signBlock = Objects.requireNonNull(signBlock, "signBlock");
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

    /**
	 * Gets the block where the [Private] sign is or will be located. Note that
	 * there are different ways to create a protection: you can right-click a chest
	 * with a sign in your hand, or you can manually place a sign and edit that. In
	 * the first case, no sign will exist yet at this location while in the second
	 * case, there will already be a sign with some text on it.
	 *
	 * @return The block where the [Private] sign is or will be located.
	 */
	public Block getSignBlock() {
		return this.signBlock;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}
}
