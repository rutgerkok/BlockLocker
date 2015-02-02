BlockLocker
===========

[View on spigotmc.org](http://www.spigotmc.org/resources/blocklocker.3268/)

Lockette/Deadbolt clone that supports UUIDs (and the new doors), written from scratch.

Current features:

* Locks chests, furnaces, doors and a few other containers using the familiar `[Private]` and `[More Users]` signs.
* You can change which block types can be protected.
* UUIDs are saved to hidden hover text data on the sign, never visible for users.
* Automatically looks up UUIDs for signs from Lockette and Deadbolt when they are read.
* All messages can be translated.
* Group support: adding `[MyGroup]` to the sign will allow anyone with the permission node `blocklocker.group.mygroup` (grant the lowercase node) and anyone in a scoreboard team or in a faction of MassiveCraft Factions called `MyGroup` (case insensitive).
* Double door support: protecting one half protects the other half, opening one half opens the other half.
* The owner of a protection can change the signs after creating them using the `/blocklocker <line number> <name>` command.
* Only the owner of a protection can destroy a protection.
* Signs placed against a container are automatically filled with `[Private]` and the name of the player.
* Admins can still open protected doors and containers.
* Automatically closing doors.
* Auto-updater, so that patches for exploits can be rolled out automatically.

Planned:

* Replace NMS access with refection or, if available at that time, an API method. (NMS is needed at the moment to get the TextComponent on the sign.)
* Integration with other plugins for group support.

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
to be around in all future versions of BlockLocker for the Bukkit API. If you need to
do something more advanced, you need to venture outside the class. Keep in mind that
your plugin might break in the future.

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
