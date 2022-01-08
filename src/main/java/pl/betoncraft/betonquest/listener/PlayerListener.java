package pl.betoncraft.betonquest.listener;

import com.taylorswiftcn.megumi.uifactory.event.screen.UIFScreenOpenEvent;
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
            playerData.getJournal().update();
            GlobalObjectives.startAll(uuid);
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
    public void onFinished(UIFScreenOpenEvent e) {
        Player player = e.getPlayer();
        String screenID = e.getScreenID();
        if (screenID.equals("questBar")) return;

        PlayerData data = BetonQuest.getInstance().getPlayerData(player.getUniqueId());
        if (data == null) return;

        data.getJournal().update();
    }
}
