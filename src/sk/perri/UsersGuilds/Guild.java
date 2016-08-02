package sk.perri.UsersGuilds;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Guild
{

    public String owner;
    public List<String> members = new ArrayList<String>();
    public List<Double> baseDouble;
    public Location base;
    public String name;
    public List<String> pending = new ArrayList<String>();
    private FileConfiguration guildsData;
    public int remove = 0; // pending to remove guild 0 = non-pending, 1 = pending, 2 = to remove

    public Guild(String name, FileConfiguration guildsData, boolean exist)
    {
        this.name = name;
        this.guildsData = guildsData;

        if (exist) loadInfo();
    }

    public void loadInfo()
    {
        this.owner = guildsData.getString(this.name+".owner");
        this.members = guildsData.getStringList(this.name+".members");

        /*this.baseDouble = guildsData.getDoubleList(this.name+".base");
        base.add(baseDouble.get(0), baseDouble.get(1), baseDouble.get(2));*/
    }

    public List getMembers()
    {
        return members;
    }

    public String getOwner()
    {
        return owner;
    }
}

