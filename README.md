BlockLocker
===========

[![Build Status](https://img.shields.io/github/actions/workflow/status/rutgerkok/BlockLocker/build.yml?branch=master)](https://github.com/rutgerkok/BlockLocker/actions/workflows/build.yml) [![Download at SpigotMC.org](https://img.shields.io/badge/download-SpigotMC.org-orange.svg)](http://www.spigotmc.org/resources/blocklocker.3268/)
[![Latest release](https://img.shields.io/github/release/rutgerkok/BlockLocker.svg)](https://github.com/rutgerkok/BlockLocker/releases)
[![Commits since latest release](https://img.shields.io/github/commits-since/rutgerkok/BlockLocker/latest.svg)](https://github.com/rutgerkok/BlockLocker/releases)

Plugin for locking individual blocks in Minecraft (like chests) using signs.

Current features:

* Locks chests, furnaces, doors and a few other containers using the familiar `[Private]` and `[More Users]` signs.
* Signs placed against a container are automatically filled with `[Private]` and the name of the player.
* Only the owner of a protection can destroy a protection.
* Admins can still open protected doors and containers.
* UUID support
  * UUIDs are saved to hidden hover text data on the sign, never visible for users.
  * Automatically looks up UUIDs for signs from Lockette and Deadbolt when they are read.
* Fully configurable
  * All messages can be translated.
  * You can change which block types can be protected. Even more complex blocks like levers work correctly.
* Group support: adding `[MyGroup]` to the sign will allow anyone with the permission node `blocklocker.group.mygroup` (grant the lowercase node) and anyone in a scoreboard team or in a faction of MassiveCraft Factions called `MyGroup` (case insensitive).
* Correctly handles complex blocks:
  * Double door support: protecting one half also protects the other half, opening one half opens the other half.
  * Doors can be set to close automatically. BlockLocker does not just toggle the open state, it will actually close the door.
  * Double chest support: protecting one half also proects the other half.
  * Trapdoor support: the sign can be attached to either the "hinge"-block or the trapdoor itself.
  * Fence gate support: the sign can be attached to either the the fence gate block or the block below.
* The owner of a protection can change the signs after creating them using the `/blocklocker <line number> <name>` command.
* Auto-updater, so that you are notified when there is a new version available.

Compilation
-----------

BlockLocker uses Maven.

* Install [Maven 3](http://maven.apache.org/download.html)
* Download this repo and run the command `mvn clean install`

After running the command there will be a file `blocklocker-XX.jar` (where `XX` is a version number) in the `target` folder.

Hooking into BlockLocker
------------------------

BlockLocker has two APIs for other plugins to use: version 1 and version 2. Right now, the only difference between them is that version 1 uses `com.google.common.base.Optional` while version 2 uses `java.util.Optional`. Version 1 was first included in BlockLocker 0.1, and the plan is to include it in all future versions of BlockLocker as long as the class `com.google.common.base.Optional` exists. Version 2 was realeased on November 17, 2019 with BlockLocker 1.7.

* If your plugin supports Minecraft 1.14, chances are that people are still using BlockLocker 1.6, and you should use API version 1.
* If your plugin supports Minecraft 1.13 or older, you'll have to use API version 1, as version 2 is not available for Minecraft 1.13.
* If your plugin only supports Minecraft 1.15 and newer, then you can safely use API version 2.

### Maven
Add the following repository to your `pom.xml` file:

```xml
<repository>
	<id>codemc-repo</id>
	<url>https://repo.codemc.org/repository/maven-public/</url>
</repository>
```

Add the following file:

```xml
<dependency>
	<groupId>nl.rutgerkok</groupId>
	<artifactId>blocklocker</artifactId>
	<version>1.9</version>
	<scope>provided</scope>
</dependency>
```

If you want to use API version 1, use BlockLocker version 0.1, otherwise for API version 2 use BlockLocker 1.7. Other BlockLocker versions are not uploaded to this repository.

### Check whether the plugin is enabled
```java
boolean enabled = Bukkit.getPluginManager().getPlugin("BlockLocker") != null;
```

### The BlockLockerAPI - version 1
The nice thing about API version 1 is that it works with all old BlockLocker versions, all the way back to version 0.1. Version 1 still uses the `com.google.common.base.Optional` class. Because Java 8 also includes an `Optional` class, it can be expected that Google's `Optional` will be removed at some point in the future. At this moment, this API will also dissappear. However, currently the `Optional` class is not deprecated by Google, so it will take at least two years for that class to get removed. So it's still safe to use this API.

Example usage:

```java
Optional<OfflinePlayer> owner = BlockLockerAPI.getOwner(block);
```

In version 0.1 these methods were available in the BlockLockerAPI class:

```
getOwner(Block block)
getOwnerDisplayName(Block block)
isAllowed(Player player, Block block, boolean serverAdminsAlwaysAllowed)
isOwner(Player player, Block block)
isProtected(Block block)
```

You can view the complete class, along with documentation, [here](https://github.com/rutgerkok/BlockLocker/blob/master/src/main/java/nl/rutgerkok/blocklocker/BlockLockerAPI.java).

If you solely use these methods, your plugin will work with *all* versions of BlockLocker from the past.

### The BlockLockerAPI - version 2
Use the class `BlockLockerAPIv2` instead of `BlockLockerAPI`. There are no differences between version 2 and version 1 yet, except that the API now uses `java.util.Optional` instead of the older `com.google.common.base.Optional`.

### More advanced functionality
To block placement of protection signs in certain areas, you can listen to the `PlayerProtectionCreateEvent` of BlockLocker or the `BlockPlaceEvent` of Bukkit. This works even for auto-placed signs.

I have kept the number of methods in BlockLockerAPI small, as each method will need
to be around in all future versions of BlockLocker. If you need to do something more
advanced, you need to venture outside the class. Keep in mind that your plugin might
break in the future. Here are some things that you can do.

Here's an example for checking whether redstone is allowed in the protection:

```java
private boolean isRedstoneAllowed(Block block) {
    BlockLockerPlugin plugin = BlockLockerAPIv2.getPlugin();
    Optional<Protection> protection = plugin.getProtectionFinder().findProtection(block);
    if (!protection.isPresent()) {
        // Not protected, so redstone is allowed to change things here
        return true;
    }
    Profile redstoneProfile = plugin.getProfileFactory().fromRedstone();
    // Will return true when [Redstone] or [Everyone] is on one of the signs
    return protection.get().isAllowed(redstoneProfile);
}
```

Here's an example of how to add a custom group system:

```java
BlockLockerAPIv2.getPlugin().getGroupSystems().addSystem(new GroupSystem() {

            @Override
            public boolean isInGroup(Player player, String groupName) {
                // TODO Auto-generated method stub
                return false;
            }});
```

Here's an example of how to add another block type as a protectable type:

```java
    BlockLockerAPIv2.getPlugin().getChestSettings().getExtraProtectables().add(new ProtectableBlocksSettings() {

            @Override
            public boolean canProtect(Block block) {
                // Return whether the block can be protected by ANY of the protection types (CONTAINER, DOOR, etc.)
		// Must be consistent with the method below
                return false;
            }

            @Override
            public boolean canProtect(ProtectionType type, Block block) {
                // Return whether the block can be protected by the given protection types (CONTAINER, DOOR, etc.)
		// Must be consistent with the method above
                return false;
            }});
```

If you believe that a method should be added to BlockLockerAPI, please create a Github issue in this repository.
