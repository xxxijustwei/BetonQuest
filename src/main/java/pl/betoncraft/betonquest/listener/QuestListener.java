package pl.betoncraft.betonquest.listener;

import net.citizensnpcs.api.CitizensAPI;
import net.sakuragame.eternal.dragoncore.api.CoreAPI;
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent;
import net.sakuragame.eternal.justmessage.api.event.quest.JournalStickEvent;
import net.sakuragame.eternal.justmessage.screen.ui.quest.ConversationScreen;
import net.sakuragame.eternal.waypoints.api.WaypointsAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.PlayerConversationStartEvent;
import pl.betoncraft.betonquest.api.event.QuestDataUpdateEvent;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.config.JournalProfile;
import pl.betoncraft.betonquest.conversation.ConversationData;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.utils.Scheduler;

import java.util.Arrays;
import java.util.UUID;

public class QuestListener implements Listener {

    public QuestListener() {
        CoreAPI.registerKey("J");
        CoreAPI.registerKey("M");
    }

    @EventHandler(ignoreCancelled = true)
    public void onDataUpdate(QuestDataUpdateEvent e) {
        UUID uuid = e.getUUID();
        String objective = e.getObjID();
        String date = e.getData();
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().updateObjective(uuid, objective, date));
    }

    @EventHandler
    public void onKeyPress(KeyPressEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (e.getKey().equals("J")) {
            BetonQuest.getInstance().getPlayerData(uuid).getJournal().openUI();
            return;
        }

        if (e.getKey().equals("M")) {
            PlayerData data = BetonQuest.getInstance().getPlayerData(uuid);
            String current = data.getJournal().getStick();
            JournalProfile profile = FileManager.getPackages().getJournal().get(current);
            if (profile == null || profile.getWaypoint() == null) return;

            WaypointsAPI.open(player, "quest", Arrays.asList("§6§l前往", "§f(%distance%m)"));
        }
    }

    @EventHandler
    public void onStart(PlayerConversationStartEvent e) {
        Player player = e.getPlayer();
        ConversationData data = e.getConversation().getData();
        if (data == null) return;

        int npcID = data.getNpcID();
        if (npcID == -1) {
            ConversationScreen.setConvNPC(player, player.getUniqueId(), data.getModelScale());
        }
        else {
            Entity entity = CitizensAPI.getNPCRegistry().getById(npcID).getEntity();
            ConversationScreen.setConvNPC(player, entity.getUniqueId(), data.getModelScale());
        }
    }

    @EventHandler
    public void onStick(JournalStickEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        BetonQuest.getInstance().getPlayerData(uuid).getJournal().setStick(e.getID());
    }

}
