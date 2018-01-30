package nl.rutgerkok.blocklocker;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import com.google.common.base.Charsets;

public class TestPlayer implements Player {

    private final String name;
    private final UUID uuid;

    /**
     * Test player with arbitrary name.
     */
    public TestPlayer() {
        this("TestPlayer");
    }

    /**
     * Test player with an uuid based on the lowercase name.
     * 
     * @param name
     *            The name.
     */
    public TestPlayer(String name) {
        this(name, UUID.nameUUIDFromBytes(name.toLowerCase().getBytes(Charsets.UTF_8)));
    }

    /**
     * Test player with given name and id.
     * 
     * @param name
     *            Name.
     * @param uuid
     *            Id.
     */
    public TestPlayer(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void acceptConversationInput(String input) {
        throw new UnsupportedOperationException();

    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        throw new UnsupportedOperationException();

    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean addPassenger(Entity arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean addPotionEffect(PotionEffect effect) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addPotionEffect(PotionEffect effect, boolean force) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> effects) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean addScoreboardTag(String arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    @Deprecated
    public void awardAchievement(org.bukkit.Achievement achievement) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean canSee(Player player) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void chat(String msg) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void closeInventory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void damage(double amount) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void damage(double amount, Entity source) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean eject() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TestPlayer)) {
            return false;
        }
        TestPlayer other = (TestPlayer) obj;
        if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects() {
        throw new UnsupportedOperationException();

    }

    @Override
    public InetSocketAddress getAddress() {
        throw new UnsupportedOperationException();

    }

    @Override
    public AdvancementProgress getAdvancementProgress(Advancement arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean getAllowFlight() {
        throw new UnsupportedOperationException();

    }

    @Override
    public AttributeInstance getAttribute(Attribute attribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location getBedSpawnLocation() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean getCanPickupItems() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Location getCompassTarget() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getCooldown(Material arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getCustomName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Inventory getEnderChest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getEntityId() {
        throw new UnsupportedOperationException();

    }

    @Override
    public EntityEquipment getEquipment() {
        throw new UnsupportedOperationException();

    }

    @Override
    public float getExhaustion() {
        throw new UnsupportedOperationException();

    }

    @Override
    public float getExp() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getExpToLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getEyeHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getEyeHeight(boolean ignoreSneaking) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location getEyeLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFallDistance() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getFireTicks() {
        throw new UnsupportedOperationException();

    }

    @Override
    public long getFirstPlayed() {
        throw new UnsupportedOperationException();

    }

    @Override
    public float getFlySpeed() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getFoodLevel() {
        throw new UnsupportedOperationException();

    }

    @Override
    public GameMode getGameMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getHealth() {
        throw new UnsupportedOperationException();

    }

    @Override
    public double getHealthScale() {
        throw new UnsupportedOperationException();

    }

    @Override
    public double getHeight() {
        throw new UnsupportedOperationException();

    }

    @Override
    public PlayerInventory getInventory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getItemInHand() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getItemOnCursor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Player getKiller() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getLastDamage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityDamageEvent getLastDamageCause() {
        throw new UnsupportedOperationException();

    }

    @Override
    public long getLastPlayed() {
        throw new UnsupportedOperationException();

    }

    @Override
    public List<Block> getLastTwoTargetBlocks(Set<Material> arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity getLeashHolder() throws IllegalStateException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getLevel() {
        throw new UnsupportedOperationException();

    }

    @Override
    public List<Block> getLineOfSight(Set<Material> arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getLocale() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Location getLocation() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Location getLocation(Location loc) {
        throw new UnsupportedOperationException();

    }

    @Override
    public MainHand getMainHand() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxFireTicks() {
        throw new UnsupportedOperationException();

    }

    @Override
    public double getMaxHealth() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getMaximumAir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaximumNoDamageTicks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getNoDamageTicks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InventoryView getOpenInventory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity getPassenger() {
        throw new UnsupportedOperationException();

    }

    @Override
    public List<Entity> getPassengers() {
        throw new UnsupportedOperationException();

    }

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Player getPlayer() {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getPlayerListName() {
        throw new UnsupportedOperationException();

    }

    @Override
    public long getPlayerTime() {
        throw new UnsupportedOperationException();

    }

    @Override
    public long getPlayerTimeOffset() {
        throw new UnsupportedOperationException();

    }

    @Override
    public WeatherType getPlayerWeather() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getPortalCooldown() {
        throw new UnsupportedOperationException();

    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getRemainingAir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getRemoveWhenFarAway() {
        throw new UnsupportedOperationException();

    }

    @Override
    public float getSaturation() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Scoreboard getScoreboard() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Set<String> getScoreboardTags() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Server getServer() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Entity getShoulderEntityLeft() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Entity getShoulderEntityRight() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getSleepTicks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity getSpectatorTarget() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatistic(Statistic statistic) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Block getTargetBlock(Set<Material> arg0, int arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTicksLived() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int getTotalExperience() {
        throw new UnsupportedOperationException();

    }

    @Override
    public EntityType getType() {
        throw new UnsupportedOperationException();

    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public Entity getVehicle() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Vector getVelocity() {
        throw new UnsupportedOperationException();

    }

    @Override
    public float getWalkSpeed() {
        throw new UnsupportedOperationException();

    }

    @Override
    public double getWidth() {
        throw new UnsupportedOperationException();

    }

    @Override
    public World getWorld() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void giveExp(int amount) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void giveExpLevels(int amount) {
        throw new UnsupportedOperationException();

    }

    @Override
    @Deprecated
    public boolean hasAchievement(org.bukkit.Achievement achievement) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasAI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCooldown(Material arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasGravity() {
        throw new UnsupportedOperationException();

    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean hasLineOfSight(Entity other) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasPermission(Permission perm) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasPermission(String name) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasPlayedBefore() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void hidePlayer(Player player) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void hidePlayer(Plugin arg0, Player arg1) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isBanned() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isBlocking() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCollidable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConversing() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isCustomNameVisible() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isDead() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isFlying() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isGliding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGlowing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHandRaised() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isHealthScaled() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isInsideVehicle() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isInvulnerable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLeashed() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isOnGround() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isOnline() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isOp() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isPermissionSet(String name) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isPlayerTimeRelative() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isSilent() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isSleeping() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSleepingIgnored() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isSneaking() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isSprinting() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean isWhitelisted() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void kickPlayer(String message) {
        throw new UnsupportedOperationException();

    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
        throw new UnsupportedOperationException();

    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean leaveVehicle() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void loadData() {
        throw new UnsupportedOperationException();

    }

    @Override
    public InventoryView openEnchanting(Location location, boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InventoryView openInventory(Inventory inventory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void openInventory(InventoryView inventory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InventoryView openMerchant(Merchant arg0, boolean arg1) {
        throw new UnsupportedOperationException();

    }

    @Override
    public InventoryView openMerchant(Villager trader, boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InventoryView openWorkbench(Location location, boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean performCommand(String command) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void playEffect(EntityEffect type) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void playEffect(Location loc, Effect effect, int data) {
        throw new UnsupportedOperationException();

    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void playNote(Location loc, byte instrument, byte note) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void playNote(Location loc, Instrument instrument, Note note) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void playSound(Location location, Sound sound, float volume, float pitch) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void playSound(Location arg0, Sound arg1, SoundCategory arg2, float arg3, float arg4) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void playSound(Location location, String sound, float volume, float pitch) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void playSound(Location arg0, String arg1, SoundCategory arg2, float arg3, float arg4) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void recalculatePermissions() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();

    }

    @Override
    @Deprecated
    public void removeAchievement(org.bukkit.Achievement achievement) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean removePassenger(Entity arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean removeScoreboardTag(String arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void resetMaxHealth() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void resetPlayerTime() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void resetPlayerWeather() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void resetTitle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveData() {
        throw new UnsupportedOperationException();

    }

    @Override
    public void sendBlockChange(Location loc, int material, byte data) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void sendBlockChange(Location loc, Material material, byte data) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean sendChunkChange(Location loc, int sx, int sy, int sz, byte[] data) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void sendMap(MapView map) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void sendMessage(String message) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void sendMessage(String[] messages) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void sendRawMessage(String message) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void sendSignChange(Location loc, String[] lines) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void sendTitle(String title, String subtitle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendTitle(String arg0, String arg1, int arg2, int arg3, int arg4) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public Map<String, Object> serialize() {
        throw new UnsupportedOperationException();

    }

    public void setAi(boolean ai) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAI(boolean ai) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAllowFlight(boolean flight) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBedSpawnLocation(Location location) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setBedSpawnLocation(Location location, boolean force) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setCanPickupItems(boolean pickup) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setCollidable(boolean collidable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCompassTarget(Location loc) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setCooldown(Material arg0, int arg1) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setCustomName(String name) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setCustomNameVisible(boolean flag) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setDisplayName(String name) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setExhaustion(float value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setExp(float exp) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setFallDistance(float distance) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setFireTicks(int ticks) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setFlying(boolean value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setFoodLevel(int value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setGameMode(GameMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGliding(boolean gliding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGlowing(boolean flag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGravity(boolean arg0) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setHealth(double health) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setHealthScale(double scale) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setHealthScaled(boolean scale) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setInvulnerable(boolean invulnerable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setItemInHand(ItemStack item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setItemOnCursor(ItemStack item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastDamage(double damage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent event) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean setLeashHolder(Entity holder) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setLevel(int level) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setMaxHealth(double health) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setMaximumAir(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaximumNoDamageTicks(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setNoDamageTicks(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOp(boolean value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean setPassenger(Entity passenger) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setPlayerListName(String name) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setPlayerWeather(WeatherType type) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setPortalCooldown(int arg0) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setRemainingAir(int ticks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRemoveWhenFarAway(boolean remove) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setResourcePack(String url) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setResourcePack(String arg0, byte[] arg1) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setSaturation(float value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setShoulderEntityLeft(Entity arg0) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setShoulderEntityRight(Entity arg0) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setSilent(boolean arg0) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setSneaking(boolean sneak) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setSpectatorTarget(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSprinting(boolean sprinting) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setTexturePack(String url) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setTicksLived(int value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setTotalExperience(int exp) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setVelocity(Vector velocity) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setWhitelisted(boolean value) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean setWindowProperty(Property prop, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showPlayer(Player player) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void showPlayer(Plugin arg0, Player arg1) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, double extra) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, double extra, T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
            double offsetY, double offsetZ, T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ, double extra) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ, double extra, T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
            double offsetZ, T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spigot spigot() {
        return new Player.Spigot();
    }

    @Override
    public void stopSound(Sound arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void stopSound(Sound arg0, SoundCategory arg1) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void stopSound(String arg0) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void stopSound(String arg0, SoundCategory arg1) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean teleport(Entity destination) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean teleport(Entity destination, TeleportCause cause) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean teleport(Location location) {
        throw new UnsupportedOperationException();

    }

    @Override
    public boolean teleport(Location location, TeleportCause cause) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void updateInventory() {
        throw new UnsupportedOperationException();
    }

}
