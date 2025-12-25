# Simple IRC

Simple IRC is a Fabric mod that allows you to connect to an IRC server in Minecraft. With it, you can fully interface with an IRC server (except for, with huge pain, services).

## Usage

- You can interact with an IRC server using game commands (use "/simpleirc help" for a list of commands).
- You can save IRC connection info through the config system using the "modmenu" mod.
- You can toggle your IRC connection with a keybind (default: "K").

To connect to the server saved in the configuration file, type the "/simpleirc connect" command without any additional parameters.

### Tested on Pissnet (UnrealIRCd, no services) and UnrealIRCd with services.

## Notes

If you can't connect to your IRC channel, make sure no one has already joined with your Minecraft name on the IRC side. If this happened, use "/simpleirc raw NICK <newnick>" and "/simpleirc join &lt;channel&gt;".
- This will also happen if you have a Minecraft name that starts with a number, you will NOT be able to connect with your Minecraft name. The technical reason is IRC reserves those for server IDs.

If you enabled autoconnect, it'll connect in 5 seconds after entering a world.

## Attribution

This mod is a fork of [SimpleIRC by BasedUser](https://github.com/BasedUser/SimpleIRC) which is a fork of [JustIRC](https://github.com/8bitFra/JustIRC).
This fork only exists because BasedUser didn't add me as a contributor.
The trinity continues in this mod also being my first modding experience or any experience in java.