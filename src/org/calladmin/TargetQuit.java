package org.calladmin;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TargetQuit implements Listener {
	
	FileConfiguration config = main.plugin.getConfig();
	
	@EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {		
		Player player = event.getPlayer();
    	//Target behaviour
		for(Player player1 : Bukkit.getServer().getOnlinePlayers())
        {
			String reporter = player1.getName();
			ConfigurationSection missingtarget = config.getConfigurationSection("Reports");
			for (String reportername : missingtarget.getKeys(false)) {
			    String target = (String) missingtarget.get(reportername + ".Target");
		    if(target == (player.getName())) {
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
                    .addField("Target Left", "Target (" + player.getName() + ") has left the server. A ban for " + config.getString("Target Ban Time") + " " + "has been applied. The report has been resolved automatically." , true)
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
            	main.plugin.getServer().dispatchCommand(main.plugin.getServer().getConsoleSender(), "tempban " + player.getName() + " " + config.getString("Target Ban Time") + " " + config.getString("Target Ban Reason"));
            	config.set("Reports." + reporter, null);
            	File save = new File(main.plugin.getDataFolder(), "config.yml");
            	try {
					config.save(save);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}           	
            }
            if ((config.getBoolean("Enable Automatic Bans?") == false) || (main.plugin.getServer().getPluginManager().getPlugin("Essentials") == null)) {
            	config.set("Reports." + reporter, null);
            	File save = new File(main.plugin.getDataFolder(), "config.yml");
            	try {
					config.save(save);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}           	
            }
		} else {
				main.plugin.getServer().dispatchCommand(main.plugin.getServer().getConsoleSender(), "Target not found!");
		}
			}
        }

}
}
