package pl.betoncraft.betonquest.listener;

import net.sakuragame.eternal.dragoncore.api.event.YamlSendToPlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.core.GlobalObjectives;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.utils.Scheduler;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final BetonQuest plugin = BetonQuest.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        Scheduler.runAsync(() -> {
            PlayerData playerData = BetonQuest.getStorageManager().getPlayerData(uuid);
            BetonQuest.getInstance().putPlayerData(uuid, playerData);
            playerData.startObjectives();
            GlobalObjectives.startAll(uuid);
            playerData.getJournal().update();
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        for (Objective objective : BetonQuest.getQuestManager().getPlayerObjectives(uuid)) {
            objective.removePlayer(uuid);
        }
        BetonQuest.getInstance().removePlayerData(uuid);
    }

    @EventHandler
    public void onSend(YamlSendToPlayerEvent e) {
        Player player = e.getPlayer();
        plugin.getConversationUI().send2Player(player);
    }
}
