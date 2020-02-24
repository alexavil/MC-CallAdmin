package org.calladmin;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Random;
import java.util.logging.Logger;

 
public class main extends JavaPlugin implements Listener {
	public static main plugin;
	public FileConfiguration config = getConfig();
	private static final Logger log = Logger.getLogger("Minecraft");
    
    
    @Override
    public void onEnable() {
    	plugin = this;
    	//Server Info value is a stock MOTD, in future it should grab the Server MOTD
    	config.addDefault("Webhook URL", "");
        config.addDefault("Webhook Avatar URL", "");
        config.addDefault("Message Content", "");
        config.addDefault("Webhook Username", "CallAdmin");
        config.addDefault("Server Info", "A Minecraft Server");
        config.addDefault("Enable Automatic Bans?", true);
        config.addDefault("Reporter Ban Time", "1d");
        config.addDefault("Target Ban Time", "1d");
        config.addDefault("Reporter Ban Reason", "Banned: CallAdmin abuse.");
        config.addDefault("Target Ban Reason", "Banned: Disconnecting while a report is active.");
        config.createSection("Reports");
        config.options().copyDefaults(true);
        saveConfig();
        @SuppressWarnings("unused")
		Metrics metrics = new Metrics(this);
        new UpdateChecker(this).checkForUpdate();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new TargetQuit(), this);
        if (getServer().getPluginManager().getPlugin("Essentials") == null) {
        	log.severe("ALERT: Essentials not found! Automatic bans will be disabled if they're on.");
        }

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
    		sender.sendMessage("[CallAdmin] This server is running CallAdmin v0.8");
    		return true;
    	}
        if (command.getName().equalsIgnoreCase("calladmin")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("I didn't think someone with Console access would ever call for help. But you're special.");
                return true;
            }
        	if (args.length == 0) {
        		sender.sendMessage("[CallAdmin] Please specify your message!");
        		return true;
        	}
        	if (args.length > 0) {     
        	Player player = Bukkit.getPlayer(sender.getName());
            String reason = "";
            for(int i = 0; i < args.length; i++){
                String arg = args[i] + " "; 
                reason = reason + arg;
            }
            String webhookurl = config.getString("Webhook URL");
            if (webhookurl.length() == 0) {
            	sender.sendMessage("[CallAdmin] Discord Webhook URL is not set! Please edit your config.yml or contact the server administrator.");
            	return true;
            }
            sender.sendMessage("[CallAdmin] Message sent to admins!");
            Random id = new Random();
            int ID = (id.nextInt(999)+1);
            String ReportID = Integer.toString(ID);
            if ((config.getBoolean("Enable Automatic Bans?") == true) && (main.plugin.getServer().getPluginManager().getPlugin("Essentials") != null)) {
            	sender.sendMessage("[CallAdmin] WARNING! You will be automatically banned if you leave the server.");
            	sender.sendMessage("[CallAdmin] Please wait until an admin arrives and resolves your report.");
            	String reporter = player.getName();
            	config.createSection("Reports." + reporter);
            	config.set("Reports." + reporter + ".Report ID", ReportID);
            	config.set("Reports." + reporter + ".Reason", reason);
            	File save = new File(getDataFolder(), "config.yml");
            	try {
					config.save(save);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}           	
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
                    .addField("Message", reason, true)
            .addField("Reporter", player.getName(), true)
            .setThumbnail("")
            .setFooter("", "")
            .setImage("")
            .setAuthor(config.getString("Server Info") + " " + "# " + ReportID + " - General Report"  , "", "")
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
    	//Report command
        if (command.getName().equalsIgnoreCase("report")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("I didn't think someone with Console access would ever call for help. But you're special.");
                return true;
            }
        	if (args.length == 0) {
        		sender.sendMessage("[CallAdmin] Please specify a player!");
        		return true;
        	}
        	if (args.length == 1) {
        		sender.sendMessage("[CallAdmin] Please specify a reason!");
        		return true;
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
            Random id = new Random();
            int ID = (id.nextInt(999)+1);
            String ReportID = Integer.toString(ID);
            if ((config.getBoolean("Enable Automatic Bans?") == true) && (main.plugin.getServer().getPluginManager().getPlugin("Essentials") != null)) {
            	sender.sendMessage("[CallAdmin] WARNING! You will be automatically banned if you leave the server.");
            	sender.sendMessage("[CallAdmin] Please wait until an admin arrives and resolves your report.");
            	String reporter = player.getName();
            	config.createSection("Reports." + reporter);
            	config.set("Reports." + reporter + ".Report ID", ReportID);
            	config.set("Reports." + reporter + ".Target", target.getName());
            	config.set("Reports." + reporter + ".Reason", reason);
            	File save = new File(getDataFolder(), "config.yml");
            	try {
					config.save(save);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}           	
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
            .setAuthor(config.getString("Server Info") + " " + "# " + ReportID + " - Player Report" , "", "")
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
            		sender.sendMessage("[CallAdmin] Please specify a reason!");
            		return true;
            	}
            	if (args.length > 1) {
            		Player player = Bukkit.getPlayer(args[0]);
            		String reason = "";
            		for(int i = 1; i < args.length; i++){
                        String arg = args[i] + " "; 
                        reason = reason + arg;
                    }
            		String reporter = player.getName();
                	ConfigurationSection report = config.getConfigurationSection("Reports." + reporter);
                	if (report != null) {
            			player.sendMessage("[CallAdmin] Your report(s) have been resolved.");           			
            			config.set("Reports." + reporter, null);
            			File save = new File(getDataFolder(), "config.yml");
                    	try {
        					config.save(save);
        				} catch (IOException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
                    	String webhookurl = config.getString("Webhook URL");
            			DiscordWebhook webhook = new DiscordWebhook(webhookurl);
                        webhook.setContent("");
                        webhook.setAvatarUrl(config.getString("Webhook Avatar URL"));
                        webhook.setUsername(config.getString("Webhook Username"));
                        webhook.setTts(false);
                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                                .setTitle("")
                                .setDescription("")
                                .setColor(Color.RED)
                                .addField("Report Resolved", "Report(s) sent by " + player.getName() + " have been resolved by " + sender.getName() + ". Reason: " + reason + "." , true)
                        .setThumbnail("")
                        .setFooter("", "")
                        .setImage("")
                        .setAuthor("", "", "")
                        .setUrl(""));
                        try {
            				webhook.execute();
            			} catch (IOException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			} //Handle exception
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
    

	@EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {		
		Player player = event.getPlayer();
		//Reporter behaviour
		String reporter = player.getName();
    	ConfigurationSection report = config.getConfigurationSection("Reports." + reporter);
    	if (report != null) {
			String webhookurl = config.getString("Webhook URL");
			DiscordWebhook webhook = new DiscordWebhook(webhookurl);
            webhook.setContent("");
            webhook.setAvatarUrl(config.getString("Webhook Avatar URL"));
            webhook.setUsername(config.getString("Webhook Username"));
            webhook.setTts(false);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle("")
                    .setDescription("")
                    .setColor(Color.RED)
                    .addField("Reporter Left", "Reporter (" + player.getName() + ") has left the server. A ban for " + config.getString("Reporter Ban Time") + " " + "has been applied." , true)
            .setThumbnail("")
            .setFooter("", "")
            .setImage("")
            .setAuthor("", "", "")
            .setUrl(""));
            try {
				webhook.execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //Handle exception
            if ((config.getBoolean("Enable Automatic Bans?") == true) && (main.plugin.getServer().getPluginManager().getPlugin("Essentials") != null)) {
            	main.plugin.getServer().dispatchCommand(main.plugin.getServer().getConsoleSender(), "tempban " + player.getName() + " " + config.getString("Reporter Ban Time") + " " + config.getString("Reporter Ban Reason"));
    			config.set("Reports." + reporter, null);
    			File save = new File(getDataFolder(), "config.yml");
            	try {
					config.save(save);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            if ((config.getBoolean("Enable Automatic Bans?") == false) || (main.plugin.getServer().getPluginManager().getPlugin("Essentials") == null)) {
            	config.set("Reports." + reporter, null);
            	File save = new File(getDataFolder(), "config.yml");
            	try {
					config.save(save);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}           	
            }
		}   	
    }		
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
		Player player = event.getPlayer();
		if(player.hasPermission("calladmin.admin")) {		
	        BukkitScheduler scheduler = main.plugin.getServer().getScheduler();
	        scheduler.scheduleSyncDelayedTask(main.plugin, new Runnable() {
	            @Override
	            public void run() {
	            	if ((config.getConfigurationSection("Reports") != null) && (config.getConfigurationSection("Reports").getKeys(false).isEmpty() == false)) {
	            		config.set("Reports", null);
	            		config.createSection("Reports");
	        				File save = new File(getDataFolder(), "config.yml");
	                    	try {
	        					config.save(save);
	        				} catch (IOException e) {
	        					// TODO Auto-generated catch block
	        					e.printStackTrace();
	        				}
	                    	String webhookurl = config.getString("Webhook URL");
	            			DiscordWebhook webhook = new DiscordWebhook(webhookurl);
	                        webhook.setContent("");
	                        webhook.setAvatarUrl(config.getString("Webhook Avatar URL"));
	                        webhook.setUsername(config.getString("Webhook Username"));
	                        webhook.setTts(false);
	                        webhook.addEmbed(new DiscordWebhook.EmbedObject()
	                                .setTitle("")
	                                .setDescription("")
	                                .setColor(Color.RED)
	                                .addField("Reports Resolved", "All active reports have been resolved automatically." , true)
	                        .setThumbnail("")
	                        .setFooter("", "")
	                        .setImage("")
	                        .setAuthor("", "", "")
	                        .setUrl(""));
	                        try {
	            				webhook.execute();
	            			} catch (IOException e) {
	            				// TODO Auto-generated catch block
	            				e.printStackTrace();
	            			} //Handle exception
	                    }
                		            }
	        }, 2400L);
	        }
    }
}
 
