package net.eq2online.permissions.plugin;

import net.eq2online.permissions.ReplicatedPermissionsContainer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * Class which monitors inbound queries in order to store useful information about them.
 *
 * @author Adam Mummery-Smith
 */
public class ReplicatedPermissionsMonitor {
    /**
     * Data store file for this provider
     */
    private final File dataFile;

    /**
     * Parent plugin
     */
    private final Plugin plugin;

    /**
     * Mod information
     */
    private final YamlConfiguration data;

    /**
     * Mod information received from the remote client, mapped to players
     */
    private final Map<String, Hashtable<String, ReplicatedPermissionsContainer>> playerModInfo = new Hashtable<>();

    /**
     * @param plugin
     */
    public ReplicatedPermissionsMonitor(Plugin plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "modinfo.yml");
        this.plugin = plugin;
        this.data = YamlConfiguration.loadConfiguration(this.dataFile);

        String dataFileHeader = "\n";
        dataFileHeader += "This file is not a configuration file.\n\n";
        dataFileHeader += "It is used to store received permissions from client mods with permissions grouped by mod name\n";
        dataFileHeader += "and version. This can be used by server admins as a quick reference to permissions supported by\n";
        dataFileHeader += "client mods, and as a way of determining which mods are in use by players connecting to the server.\n";

        this.data.options().header(dataFileHeader);
    }

    /**
     * Called when a player registers a channel, adds the player to the list of local players
     *
     * @param player
     */
    public void addPlayer(Player player) {
        this.playerModInfo.put(player.getName(), new Hashtable<String, ReplicatedPermissionsContainer>());
    }

    /**
     * Called when a player disconnects, removes the player metadata from the store
     *
     * @param player
     */
    public void removePlayer(Player player) {
        this.playerModInfo.remove(player.getName());
    }

    /**
     * Called when a query is received from a remote player, stores information about the query in the metadata store
     *
     * @param player
     * @param query
     */
    public void onQuery(Player player, ReplicatedPermissionsContainer query) {
        if (query != null) {
            if (!this.playerModInfo.containsKey(player.getName())) {
                this.plugin.getLogger().warning("Received query from unregistered player " + player.getName());
                this.playerModInfo.put(player.getName(), new Hashtable<String, ReplicatedPermissionsContainer>());
            }

            // Log the query, it can be used to get information about a players mods later on, as well
            // as being used in case the "refresh" command is given as a kind of query cache.
            this.playerModInfo.get(player.getName()).put(query.modName, query);

            if (query.permissions.size() > 0) {
                ConfigurationSection versionSection = getModVersionSection(query.modName, query.modVersion);

                if (versionSection != null) {
                    versionSection.set("permissions", new ArrayList<>(query.permissions));

                    try {
                        this.data.save(this.dataFile);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    /**
     * @param modName
     * @return
     */
    public ConfigurationSection getModSection(String modName) {
        String modPath = "mods." + modName;
        ConfigurationSection modSection = this.data.getConfigurationSection(modPath);
        if (modSection == null) {
            modSection = this.data.createSection(modPath);
        }

        return modSection;
    }

    /**
     * @param modName
     * @param modVersion
     * @return
     */
    public ConfigurationSection getModVersionSection(String modName, Float modVersion) {
        ConfigurationSection modSection = this.getModSection(modName);
        String modVersionPath = this.getVersionPath(modVersion);
        ConfigurationSection versionSection = modSection.getConfigurationSection(modVersionPath);
        if (versionSection == null) {
            return modSection.createSection(modVersionPath);
        }

        return null;
    }

    /**
     * @param modVersion
     * @return
     */
    public String getVersionPath(Float modVersion) {
        return String.format("%.3f", modVersion).replace('.', '_');
    }

    /**
     * @return the playerModInfo
     */
    public Map<String, Hashtable<String, ReplicatedPermissionsContainer>> getPlayerModInfo() {
        return this.playerModInfo;
    }
}
