/**
 * FINAL VERSION 0.0.1 (plugin alpha) SHITTY CODE! NEW CODE IN NEW VERSION!
 *
 * Tento kod nie je este plna ani dokoncena verzia. Je to zadial len nacrt, taky hovno-kod preto
 * sa mozno zda nelogicky a spravy pre sendera su kazda ina... Tento kod bodem doplnat a obnovovat
 *
 * This is not a final version of code. This is only alpha some a shitty-code. This code is being
 * refreshed.
 */

package sk.perri.UsersGuilds;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersGuilds extends JavaPlugin
{

    public Map<String, Guild> guilds = new HashMap<>();
    public Map<String, Guild> pendingInvite = new HashMap<>();
    public Map<String, Guild> pendingRemove = new HashMap<>();
    public Map<String, Guild> players = new HashMap<>();
    public File guildsFile = new File(getDataFolder(), "guilds.yml");

    public FileConfiguration guildsData;

    @Override
    public void onEnable()
    {
        //load settings
        if (!guildsFile.exists()) //load guilds
        {
            getLogger().info("[UsersGuilds] guild.yml not found! Create new!");

            guildsFile.getParentFile().mkdirs();

            try {
                if (guildsFile.createNewFile())
                {
                    getLogger().info("[UsersGuilds] guild.yml has been created successfully!");
                }
                else
                {
                    getLogger().info("[UsersGuilds] guild.yml has been NOT created!");
                    onDisable();
                    System.exit(1);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        loadGuilds();

        this.getCommand("ug").setExecutor(new UsersGuildsCommandExecutor(this)); //nastavujem listener na prikazy

        getLogger().info("Plugin has been enabled!");
    }

    @Override
    public void onDisable()
    {
        //save guilds
        this.pendingInvite.clear();
        this.guilds.clear();
        this.players.clear();
        this.pendingRemove.clear();
        getLogger().info("Plugin has been disabled!");
    }

    public void pluginReload(CommandSender sender)
    {
        this.onDisable();
        this.reloadConfig();
        this.onEnable();
        sender.sendMessage(ChatColor.GRAY+"[UsersGuild] Plugin reloaded!");
    }

    private void loadGuilds()
    {
        guildsData = YamlConfiguration.loadConfiguration(guildsFile);
        for(String s : guildsData.getKeys(false))
        {
            guilds.put(s, new Guild(s, guildsData, true));
            for (String p : guilds.get(s).members)
            {
                players.put(p, guilds.get(s));
            }
            getLogger().info("Load guild: " + s + " mem count: "+ guilds.get(s).members.size());
        }
    }

    private void saveGuilds()
    {
        try
        {
            guildsData.save(guildsFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
