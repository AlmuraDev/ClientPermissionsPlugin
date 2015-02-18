package net.eq2online.permissions.plugin;

import java.util.List;

/**
 * @author Adam Mummery-Smith
 */
public interface ReplicatedPermissionsProvider {
    boolean addMod(String modName);

    boolean removeMod(String modName);

    boolean setMinModVersion(String modName, Float modVersion);

    Float getMinModVersion(String modName);

    List<String> getMods();
}
