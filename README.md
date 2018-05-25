BlockLocker
===========

[![Build Status](https://travis-ci.com/rutgerkok/BlockLocker.svg?branch=master)](https://travis-ci.com/rutgerkok/BlockLocker) [![Download at SpigotMC.org](https://img.shields.io/badge/download-SpigotMC.org-orange.svg)](http://www.spigotmc.org/resources/blocklocker.3268/)
![Latest release](https://img.shields.io/github/release/rutgerkok/BlockLocker.svg)
![Commits since latest release](https://img.shields.io/github/commits-since/rutgerkok/BlockLocker/latest.svg)

Lockette/Deadbolt clone that properly supports UUIDs (and the new doors), written from scratch.

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

### Maven
Add the following repository to your `pom.xml` file:

```xml
<repository>
	<id>rutger-repo</id>
	<url>http://www.rutgerkok.nl/repo</url>
</repository>
```

Add the following file:

```xml
<dependency>
	<groupId>nl.rutgerkok</groupId>
	<artifactId>blocklocker</artifactId>
	<version>0.1</version>
	<scope>provided</scope>
</dependency>
```

### Check whether the plugin is enabled
```java
boolean enabled = Bukkit.getPluginManager().getPlugin("BlockLocker") != null;
```

### The BlockLockerAPI class
This class contains some static methods that will never be removed. If don't want your plugin to be broken by future BlockLocker updates, use this class.

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

If you solely use these methods, your plugin will work with *all* versions of BlockLocker from the future and past.

### More advanced functionality
I have kept the number of methods in BlockLockerAPI small, as each method will need
to be around in all future versions of BlockLocker. If you need to do something more
advanced, you need to venture outside the class. Keep in mind that your plugin might
break in the future.

Here's an example for checking whether redstone is allowed in the protection:

```java
private boolean isRedstoneAllowed(Block block) {
    BlockLockerPlugin plugin = BlockLockerAPI.getPlugin();
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

If you believe that a method should be added to BlockLockerAPI, please contact me.
(See my Github profile for my e-mail.)
