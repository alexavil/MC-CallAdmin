package org.calladmin;

import java.awt.Color;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.calladmin.main;

public class EventListener implements Listener {
	
	FileConfiguration config = main.plugin.getConfig();
	
	
	@EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
		Player player = event.getPlayer();
		if(player.hasPermission("calladmin.reporter")) {
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
                    .addField("Reporter Left", "Reporter (" + player.getName() + ") has left the server. A ban for " + config.getString("Ban Time") + " " + "has been applied." , true)
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
            if ((config.getBoolean("Ban Reporter on Leave?") == true) && (main.plugin.getServer().getPluginManager().getPlugin("Essentials") != null)) {
            	main.plugin.getServer().dispatchCommand(main.plugin.getServer().getConsoleSender(), "tempban " + player.getName() + " " + config.getString("Ban Time") + " " + config.getString("Ban Reason"));
            	main.perms.playerRemove(player,"calladmin.reporter");
            }
            if ((config.getBoolean("Ban Reporter on Leave?") == false) || (main.plugin.getServer().getPluginManager().getPlugin("Essentials") == null)) {
            	main.perms.playerRemove(player,"calladmin.reporter");
            }
		}
    }
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
		Player player = event.getPlayer();
		if(player.hasPermission("calladmin.admin")) {
			main.perms.playerRemove(player,"calladmin.reporter");
	        BukkitScheduler scheduler = main.plugin.getServer().getScheduler();
	        scheduler.scheduleSyncDelayedTask(main.plugin, new Runnable() {
	            @Override
	            public void run() {
	            	for(Player player : Bukkit.getServer().getOnlinePlayers())
	                {
	                    if (player.hasPermission("calladmin.reporter")) {
	                    	main.perms.playerRemove(player,"calladmin.reporter");
	                    	player.sendMessage("[CallAdmin] Your report has been resolved.");
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
	                                .addField("Report Resolved", "Report sent by " + player.getName() + " " + "has been resolved automatically." , true)
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
	            }
	        }, 2400L);			
			
		}
    }

}
