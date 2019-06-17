package org.calladmin;
import java.awt.Color;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
 
public class main extends JavaPlugin {
	public static main plugin;
	FileConfiguration config = getConfig();
 
    @Override
    public void onEnable() {
    	plugin = this;
    	//Server Info value is a stock MOTD, in future it should grab the Server MOTD
    	config.addDefault("Webhook URL", "");
        config.addDefault("Webhook Avatar URL", "");
        config.addDefault("Message Content", "");
        config.addDefault("Webhook Username", "CallAdmin");
        config.addDefault("Server Info", "A Minecraft Server");
        config.options().copyDefaults(true);
        saveConfig();

    }
   
    @Override
    public void onDisable() {
       
    }
   
    @Override
    public boolean onCommand(CommandSender sender,
            Command command,
            String label,
            String[] args) {
    	if (command.getName().equalsIgnoreCase("cainfo")) {
    		sender.sendMessage("[CallAdmin] This server is running CallAdmin v0.5-beta1");
    		return true;
    	}
    	//Main command
        if (command.getName().equalsIgnoreCase("calladmin")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("I didn't think someone with Console access would ever call for help. But you're special.");
                return true;
            }
        	if (args.length == 0) {
        		sender.sendMessage("[CallAdmin] Please specify a player!");
        	}
        	if (args.length == 1) {
        		sender.sendMessage("[CallAdmin] Please specify a reason!");
        	}
        	if (args.length > 1) {
        	Player player = Bukkit.getPlayer(sender.getName());
            Player target = Bukkit.getPlayer(args[0]);
            String reason = ""; 
            for(int i = 1; i < args.length; i++){
                String arg = args[i] + " "; 
                reason = reason + arg;
            }
            if(target == null) {
            	sender.sendMessage("[CallAdmin] Player not found!");
            	return true;
            }
            String webhookurl = config.getString("Webhook URL");
            if (webhookurl.length() == 0) {
            	sender.sendMessage("[CallAdmin] Discord Webhook URL is not set! Please edit your config.yml or contact the server administrator.");
            	return true;
            }
            if(player == target) {
            	sender.sendMessage("[CallAdmin] You can't report yourself!");
            	return true;
            }
            if(target.hasPermission("calladmin.exempt")) {
            	sender.sendMessage("[CallAdmin] You can't report this player!");
            	return true;
            }
            sender.sendMessage("[CallAdmin] Message sent to admins!");
            DiscordWebhook webhook = new DiscordWebhook(webhookurl);
            webhook.setContent(config.getString("Message Content"));
            webhook.setAvatarUrl(config.getString("Webhook Avatar URL"));
            webhook.setUsername(config.getString("Webhook Username"));
            webhook.setTts(false);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("")
                    .setDescription("")
                    .setColor(Color.RED)
                    .addField("Reason", reason, true)
            .addField("Reporter", player.getName(), true)
            .addField("Target", target.getName(), false)
            .setThumbnail("")
            .setFooter("", "")
            .setImage("")
            .setAuthor(config.getString("Server Info"), "", "")
            .setUrl(""));
            try {
				webhook.execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //Handle exception
            return true;
        	}
        	return false;
        }
		return false;
    }
}