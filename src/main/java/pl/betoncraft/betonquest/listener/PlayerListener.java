package pl.betoncraft.betonquest.listener;

import com.taylorswiftcn.megumi.uifactory.event.screen.UIFScreenOpenEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.api.event.QuestAccountLoadedEvent;
import pl.betoncraft.betonquest.core.GlobalObjectives;
import pl.betoncraft.betonquest.core.PlayerData;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final BetonQuest plugin = BetonQuest.getInstance();

    @EventHandler(priority = EventPriority.HIGH)
    public void onFirst(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        UUID uuid = e.getUniqueId();
        PlayerData account = BetonQuest.getStorageManager().getPlayerData(uuid);
        BetonQuest.getInstance().putPlayerData(uuid, account);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSecond(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        BetonQuest.getInstance().removePlayerData(e.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerData account = BetonQuest.getInstance().getPlayerData(uuid);

        if (account == null) {
            e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            e.setKickMessage("账户数据未被正确加载，请重新进入游戏。");
            return;
        }

        QuestAccountLoadedEvent event = new QuestAccountLoadedEvent(player);
        Bukkit.getPluginManager().callEvent(event);

        account.startObjectives();
        account.getJournal().update();
        GlobalObjectives.startAll(uuid);
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
    public void onOpen(UIFScreenOpenEvent e) {
        Player player = e.getPlayer();
        String screenID = e.getScreenID();
        if (!screenID.equals("questBar")) return;

        PlayerData data = BetonQuest.getInstance().getPlayerData(player.getUniqueId());
        if (data == null) return;

        data.getJournal().update();
    }
}
