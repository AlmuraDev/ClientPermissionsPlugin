package net.eq2online.permissions.plugin;

import net.eq2online.permissions.ReplicatedPermissionsContainer;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class which manages replicating permissions to clients
 *
 * @author Adam Mummery-Smith
 */
public class ReplicatedPermissionsManagerImpl implements ReplicatedPermissionsManager {
    /**
     * Plugin which owns this manager
     */
    private ReplicatedPermissionsPlugin parent;

    /**
     * List of permission providers
     */
    private List<ReplicatedPermissionsMappingProvider> mappingProviders = new ArrayList<>();

    public ReplicatedPermissionsManagerImpl(ReplicatedPermissionsPlugin parent) {
        this.parent = parent;
    }

    @Override
    public void addMappingProvider(ReplicatedPermissionsMappingProvider provider) {
        if (!this.mappingProviders.contains(provider)) {
            this.mappingProviders.add(0, provider);
        }
    }

    @Override
    public void initAllMappingProviders() {
        for (ReplicatedPermissionsMappingProvider provider : this.mappingProviders) {
            provider.initProvider(this.parent);
        }
    }

    @Override
    public boolean checkVersion(Player player, ReplicatedPermissionsContainer data) {
        if (data.modName.equals("all")) return true;

        for (ReplicatedPermissionsMappingProvider provider : this.mappingProviders) {
            if (!provider.checkVersion(this.parent, player, data) && !player.hasPermission(ReplicatedPermissionsPlugin.ADMIN_PERMISSION_NODE))
                return false;
        }

        return true;
    }

    @Override
    public void replicatePermissions(Player player, ReplicatedPermissionsContainer data) {
        Set<String> replicatePermissions = new HashSet<>();
        boolean havePermissions = false;

        for (ReplicatedPermissionsMappingProvider provider : this.mappingProviders) {
            if (provider.providesMappingsFor(data)) {
                havePermissions = true;

                List<String> permissions = provider.getPermissions(this.parent, player, data);
                replicatePermissions.addAll(permissions);
                break;
            }
        }

        if (havePermissions) {
            ReplicatedPermissionsContainer replContainer = new ReplicatedPermissionsContainer(data.modName, data.modVersion, replicatePermissions);

            final byte[] bytes = replContainer.getBytes();
            player.sendPluginMessage(this.parent, ReplicatedPermissionsContainer.CHANNEL, ((ByteBuffer) ByteBuffer.allocate(bytes.length + 1).put((byte) 0).put(bytes).flip()).array());
        }
    }
}
