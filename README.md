BlockLocker
===========

Lockette/Deadbolt clone that supports UUIDs (and the new doors), written from scratch.

Current features:

* Locks chests, furnaces, doors and a few other containers using the familiar `[Private]` and `[More Users]` signs.
* You can change which block types can be protected.
* UUIDs are saved to hidden hover text data on the sign, never visible for users.
* Automatically looks up UUIDs for signs from Lockette and Deadbolt when they are read.
* All messages can be translated.
* Group support: adding `[MyGroup]` to the sign will allow anyone with the permission node `blocklocker.group.mygroup` (grant the lowercase node) and anyone in a scoreboard team called `MyGroup` (case insensitive).
* Double door support: protecting one half protects the other half, opening one half opens the other half.
* The owner of a protection can change the signs after creating them using the `/blocklocker <line number> <name>` command.
* Only the owner of a protection can destroy a protection.
* Signs placed against a container are automatically filled with `[Private]` and the name of the player.
* Admins can still open protected doors and containers.

Not yet implemented, planned for first release:

* Automatically closing doors.
* Ultra-simple API for third-party plugins, this API will be supported forever.

Planned after first release:

* Auto-updater, so that patches for exploits can be rolled out automatically.
* Replace NMS access with refection or, if available at that time, an API method. (NMS is needed at the moment to get the TextComponent on the sign.)
* Maybe integration with other plugins for group support?
