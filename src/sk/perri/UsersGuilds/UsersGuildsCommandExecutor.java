package sk.perri.UsersGuilds;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class UsersGuildsCommandExecutor implements CommandExecutor
{
    private final UsersGuilds plugin;

    public UsersGuildsCommandExecutor(UsersGuilds plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        //rozdelit prikazy

        if (args.length > 0)
        {
            switch(args[0])
            {
                case "list": listGuild(sender); break;
                case "yes": confirm(sender); break;
                case "no": deny(sender); break;
                case "reload": plugin.pluginReload(); break;
                case "debug": printDebug(sender); break;
            }
        }

        if (args.length > 1)
        {
            switch(args[0])
            {
                case "create": createGuild(sender, args); break;
                case "remove": removeRequest(sender, args); break;
                case "invite": inviteToGuild(sender, args); break;
                case "kick": kickFromGuild(sender, args); break;
                case "set": setGuild(sender, args); break;
                case "guild": infoGuild(sender, args); break;
                default: return false;
            }
        }

        return true;
    }

    private void saveGuilds()
    {
        try
        {
            plugin.guildsData.save(plugin.guildsFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void createGuild(CommandSender sender, String[] args)
    {
        boolean isFree = true;
        Guild tempGuild;

        sender.sendMessage(ChatColor.GOLD+"[UsersGuilds] Trying invoke command \"create\"!");

        if(!plugin.players.isEmpty())
        {
            for(String s : plugin.players.keySet()) //Check if player is not in another guild
            {
                if(sender.getName().equalsIgnoreCase(s))
                {
                    sender.sendMessage(ChatColor.RED + "[UsersGuilds] You are in the other guild!");
                    isFree = false;
                    break;
                }
            }
        }

        for(String s : plugin.guilds.keySet())
        {
            if(args[1].equalsIgnoreCase(s))
            {
                sender.sendMessage(ChatColor.RED + "[UsersGuilds] This guild name already exist!");
                isFree = false;
                break;
            }
        }

        if(isFree)
        {
            plugin.guilds.put(args[1], new Guild(args[1], plugin.guildsData, false));
            plugin.guilds.get(args[1]).owner = sender.getName();
            plugin.players.put(sender.getName(), plugin.guilds.get(args[1]));
            plugin.guildsData.set(args[1]+".owner", sender.getName());
            plugin.guildsData.set(args[1]+".members", Arrays.asList(sender.getName()));

            saveGuilds();

            sender.sendMessage(ChatColor.GREEN + "[UsersGuilds] Guild created!");
        }
    }

    private void removeRequest(CommandSender sender, String[] args)
    {
        boolean isFree = false;
        String guildName = "";

        for(String s : plugin.guilds.keySet())
        {
            if(args[1].equalsIgnoreCase(s))
            {
                isFree = true;
                guildName = s;
                break;
            }
        }

        if (!isFree)
        {
            sender.sendMessage(ChatColor.RED + "[UsersGuilds] This guild does not exist!");
        }
        else
        {
            if(plugin.guilds.get(guildName).owner.equalsIgnoreCase(sender.getName()))
            {
                plugin.guilds.get(guildName).remove = 1;
                plugin.pendingRemove.put(sender.getName(), plugin.guilds.get(guildName));
                sender.sendMessage(ChatColor.GOLD + "[UsersGuilds] Do you really remove this guild? Type /ug yes to remove this guild");
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "[UsersGuilds] You are not owner of this guild!");
            }
        }
    }

    private void inviteToGuild(CommandSender sender, String[] args)
    {
        boolean isFree = false;
        Guild tempGuild = null;

        for(String s : plugin.players.keySet())
        {
            if(sender.getName().equalsIgnoreCase(s))
            {
                isFree = true;
                tempGuild = plugin.players.get(sender.getName());
                break;
            }
        }

        if(!isFree)
        {
            sender.sendMessage(ChatColor.RED + "[UsersGuilds] You are not in any guild!");
        }
        else
        {
            if(tempGuild.owner.equalsIgnoreCase(sender.getName()))
            {
                boolean plOnline = false;
                for(Player pl : Bukkit.getOnlinePlayers())
                {
                    if (args[1].equalsIgnoreCase(pl.getName()) || args[1].equalsIgnoreCase(pl.getDisplayName()))
                    {
                        plOnline = true;
                        if(plugin.players.containsKey(pl.getName()))
                        {
                            sender.sendMessage(ChatColor.RED+"[UsersGuilds] This player is in another guild!");
                        }
                        else
                        {
                            plugin.pendingInvite.put(args[1], tempGuild);
                            pl.sendMessage(ChatColor.GOLD+"[UsersGuilds] Player "+sender.getName()+ChatColor.GOLD+
                                    " invited you to guild "+tempGuild.name+ChatColor.GOLD+". To accept type /ug yes or /ug no to deny.");
                        }

                        break;
                    }
                }

                if(!plOnline)
                    sender.sendMessage(ChatColor.RED+"[UserGuild] Player is not online!");
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "[UsersGuilds] You are not owner of this guild!");
            }
        }
    }

    private void kickFromGuild(CommandSender sender, String[] args)
    {
        boolean isErr = true;

        for(String s : plugin.players.keySet() )
        {
            if(sender.getName().equalsIgnoreCase(s))
            {
                isErr = false;
                break;
            }
        }

        if (isErr)
            sender.sendMessage(ChatColor.RED+"[UserGuild] You are not in any guild!");
        else
            if(plugin.players.get(sender.getName()).owner.equalsIgnoreCase(sender.getName()))
            {
                boolean fP = false;
                String plr = null;
                for(String ss : plugin.players.get(sender.getName()).members)
                {
                    if(ss.equalsIgnoreCase(args[1]))
                    {
                        List<String> mem = plugin.guildsData.getStringList(plugin.players.get(sender.getName()).name + ".members");
                        mem.remove(ss);
                        plr = ss;
                        plugin.guildsData.set(plugin.players.get(sender.getName()).name + ".members", mem);
                        saveGuilds();
                        sender.sendMessage(ChatColor.GOLD + "[UserGuild] Player " + ss + ChatColor.GOLD + " has been kicked from the guild!");
                        Player obet = Bukkit.getPlayer(ss);
                        if (obet != null)
                            obet.sendMessage(ChatColor.GOLD + "[UserGuild] You has been kicked from the guild!");
                    }
                }
                plugin.players.get(sender.getName()).members.remove(plr);
                plugin.players.remove(plr);
            }
            else
            {
                sender.sendMessage(ChatColor.RED+"[UserGuild] You are not owner of this guild!");
            }
    }

    private void setGuild(CommandSender sender, String[] args)
    {


    }

    private void infoGuild(CommandSender sender, String[] args)
    {
        Guild info = plugin.guilds.get(args[1]);

        sender.sendMessage(ChatColor.GOLD+"Guild name: "+ChatColor.BLUE+info.name);
        sender.sendMessage(ChatColor.GOLD+"Owner: "+ChatColor.BLUE+info.owner);

        String stringMembers = ChatColor.GOLD+"Members: "+ChatColor.BLUE;
        for(Object s : info.getMembers())
        {
            stringMembers += s + " ";
        }

        sender.sendMessage(stringMembers);
    }

    private void listGuild(CommandSender sender)
    {
        sender.sendMessage(ChatColor.GOLD+"List of all guilds, total "+plugin.guilds.size()+" guilds:");
        for (Map.Entry<String, Guild> entry : plugin.guilds.entrySet())
        {
            sender.sendMessage(ChatColor.GOLD+entry.getKey()+" - owner: "+entry.getValue().owner );
        }
    }

    private void printDebug(CommandSender sender)
    {
        sender.sendMessage("Guilds: "+plugin.guilds.toString());
        sender.sendMessage("Players: "+plugin.players.toString());
        sender.sendMessage("Pending invitions: "+plugin.pendingInvite.toString());
    }

    private void confirm(CommandSender sender)
    {
        boolean isErr = true;
        if(plugin.pendingInvite.containsKey(sender.getName()))
        {
            plugin.pendingInvite.get(sender.getName()).members.add(sender.getName());
            plugin.players.put(sender.getName(), plugin.pendingInvite.get(sender.getName()));
            plugin.pendingInvite.remove(sender.getName());
            List<String> mem = plugin.guildsData.getStringList(plugin.players.get(sender.getName()).name+".members");
            mem.add(sender.getName());
            plugin.guildsData.set(plugin.players.get(sender.getName()).name+".members", mem);
            saveGuilds();
            isErr = false;
            sender.sendMessage(ChatColor.GREEN+"[UserGuild] You are now member of guild "+plugin.players.get(sender.getName()).name);
            Player own = Bukkit.getPlayer(plugin.players.get(sender.getName()).getOwner());
            if(own != null)
                own.sendMessage(ChatColor.GREEN+"[UserGuild] Player "+sender.getName()+ChatColor.GREEN+" accept your invite.");
        }

        if(plugin.pendingRemove.containsKey(sender.getName()))
        {
            removeGuild(sender, plugin.pendingRemove.get(sender.getName()));
            isErr = false;
            sender.sendMessage(ChatColor.GREEN+"[UserGuild] Guild has been successfully removed!");
        }

        if(isErr)
            sender.sendMessage(ChatColor.RED+"[UserGuild] No pending decisions!");
    }

    private void removeGuild(CommandSender sender, Guild guild)
    {
        String guildName = guild.name;
        plugin.guildsData.set(guildName, null);
        plugin.pendingRemove.remove(sender.getName());
        for(String ss : guild.members)
            plugin.players.remove(ss);
        plugin.guilds.replace(guildName, guild, null);
        plugin.guilds.remove(guildName);

        saveGuilds();
    }

    private void deny(CommandSender sender)
    {
        boolean isErr = true;
        if(plugin.pendingInvite.containsKey(sender.getName()))
        {
            plugin.pendingInvite.remove(sender.getName());
            Player own = Bukkit.getPlayer(plugin.pendingInvite.get(sender.getName()).getOwner());
            if(own != null)
                own.sendMessage(ChatColor.GOLD+"[UserGuild] Player "+sender.getName()+ChatColor.GOLD
                        +" denied your invite.");
            sender.sendMessage(ChatColor.GOLD+"[UsersGuilds] You denied the invite to guild");
            isErr = false;
        }

        if(plugin.pendingRemove.containsKey(sender.getName()))
        {
           plugin.pendingRemove.get(sender.getName()).remove = 0;
            isErr = false;
            sender.sendMessage(ChatColor.GREEN+"[UserGuild] Request to remove has been denied!");
        }

        if (isErr)
            sender.sendMessage(ChatColor.RED+"[UserGuild] No pending decisions!");
    }
}
