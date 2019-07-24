package org.calladmin;
import java.awt.Color;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;
 
public class main extends JavaPlugin {
	public static main plugin;
	public FileConfiguration config = getConfig();
	private static final Logger log = Logger.getLogger("Minecraft");
	static Permission perms = null;
 
    @Override
    public void onEnable() {
    	plugin = this;
    	//Server Info value is a stock MOTD, in future it should grab the Server MOTD
    	config.addDefault("Webhook URL", "");
        config.addDefault("Webhook Avatar URL", "");
        config.addDefault("Message Content", "");
        config.addDefault("Webhook Username", "CallAdmin");
        config.addDefault("Server Info", "A Minecraft Server");
        config.addDefault("Ban Reporter on Leave?", true);
        config.addDefault("Ban Time", "1d");
        config.addDefault("Ban Reason", "Banned: CallAdmin abuse.");
        config.options().copyDefaults(true);
        saveConfig();
        @SuppressWarnings("unused")
		Metrics metrics = new Metrics(this);
        new UpdateChecker(this).checkForUpdate();
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            log.severe("ALERT: Vault not found! Plugin will not start.");            
        }
        if (getServer().getPluginManager().getPlugin("Essentials") == null) {
        	log.severe("ALERT: Essentials not found! Automatic bans will be disabled if they're on.");
        }
        setupPermissions();

    }
    
   
    @Override
    public void onDisable() {
       
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
   
    @Override
    public boolean onCommand(CommandSender sender,
            Command command,
            String label,
            String[] args) {
    	if (command.getName().equalsIgnoreCase("cainfo")) {
    		sender.sendMessage("[CallAdmin] This server is running CallAdmin v0.6");
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
            sender.sendMessage("[CallAdmin] Reported a player!");
            if ((config.getBoolean("Ban Reporter on Leave?") == true) && (main.plugin.getServer().getPluginManager().getPlugin("Essentials") != null)) {
            	sender.sendMessage("[CallAdmin] WARNING! You will be automatically banned if you leave the server.");
            	sender.sendMessage("[CallAdmin] Please wait until an admin arrives and resolves your report.");
                perms.playerAdd(player,"calladmin.reporter");
            }
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
            .setAuthor(config.getString("Server Info") + "", "", "")
            .setUrl(""));
            try {
				webhook.execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //Handle exception
            return true;
        	}
        }
    	if (command.getName().equalsIgnoreCase("caresolve")) {
    		if (sender.hasPermission("calladmin.admin")) {
    			if (args.length == 0) {
            		sender.sendMessage("[CallAdmin] Please specify a player!");
            		return true;
            	}
            	if (args.length == 1) {
            		Player player = Bukkit.getPlayer(args[0]);
            		if(player.hasPermission("calladmin.reporter")) {
            			perms.playerRemove(player,"calladmin.reporter");
            			player.sendMessage("[CallAdmin] Your report has been resolved.");
            			return true;
    		} else {
    			sender.sendMessage("[CallAdmin] No pending reports from this player!");
    			return true;
    		}
            	}
    		} else {
    			sender.sendMessage("[CallAdmin] You do not have permission to do that!");
    			return true;
    		}
    	}
		return false;
    }
}