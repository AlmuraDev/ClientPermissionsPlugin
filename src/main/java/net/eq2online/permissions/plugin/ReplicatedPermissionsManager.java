package net.eq2online.permissions.plugin;

import net.eq2online.permissions.ReplicatedPermissionsContainer;
import org.bukkit.entity.Player;

public interface ReplicatedPermissionsManager {
    /**
     * Add a mapping provider to the provider set
     *
     * @param provider
     */
    void addMappingProvider(ReplicatedPermissionsMappingProvider provider);

    /**
     * Initialise all registered providers
     */
    void initAllMappingProviders();

    /**
     * Check the version of the specified permissions container
     *
     * @param player
     * @param data
     * @return
     */
    boolean checkVersion(Player player, ReplicatedPermissionsContainer data);

    /**
     * If the version check succeeds, replicate the permissions to the client
     *
     * @param player
     * @param data
     */
    void replicatePermissions(Player player, ReplicatedPermissionsContainer data);

}