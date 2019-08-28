# MC-CallAdmin
Experimental Minecraft CallAdmin plugin with Discord WebHook support

# How to install
1. Grab the jar from Releases tab and drag it to your plugins folder.
2. (Re)start the server. A default config.yml should appear.
3. Open the config.yml and insert your info in it. Configure the Automatic Abuse Bans settings if needed.
4. Restart the server to apply the changes.

# Notes
The default message is set to @here. You can edit it in the config.

Setting the webhook URL is mandatory for the plugin to work. Default username is set to CallAdmin and Default Server Info is set to "A Minecraft Server". You can leave the webhook avatar blank if you don't want to use it.

Negate the calladmin.reporter permission node for groups which have all permissions (a * node). This permission is used as a flag to assign Automatic Abuse Bans. When negating, put - -calladmin.reporter **before** the '*' node.
