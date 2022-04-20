package pl.betoncraft.betonquest.listener;

import ink.ptms.adyeshach.api.AdyeshachAPI;
import ink.ptms.adyeshach.common.entity.EntityInstance;
import net.sakuragame.eternal.dragoncore.api.CoreAPI;
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent;
import net.sakuragame.eternal.justmessage.api.event.quest.JournalStickEvent;
import net.sakuragame.eternal.justmessage.screen.ui.quest.ConversationScreen;
import net.sakuragame.eternal.waypoints.api.WaypointsAPI;
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
import java.util.List;
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

            String[] args = profile.getWaypoint().split(":");
            String type = args[0];
            String id = args[1];

            List<String> label = Arrays.asList("§6§l前往", "§f(%distance%m)");
            switch (type) {
                case "npc":
                    int npcID = Integer.parseInt(id);
                    WaypointsAPI.open(player, npcID, 5, label);
                    return;
                case "point":
                    WaypointsAPI.open(player, id, label);
            }
        }
    }

    @EventHandler
    public void onStart(PlayerConversationStartEvent e) {
        Player player = e.getPlayer();
        ConversationData data = e.getConversation().getData();
        if (data == null) return;

        String npcID = data.getNpcID();
        if (npcID.equals("-1")) {
            ConversationScreen.setConvNPC(player, player.getUniqueId(), data.getModelScale());
        } else {
            EntityInstance entityFromId = AdyeshachAPI.INSTANCE.getEntityFromId(npcID, player);
            if (entityFromId == null) return;

            ConversationScreen.setConvNPC(player, entityFromId.getNormalizeUniqueId(), data.getModelScale());
        }
    }

    @EventHandler
    public void onStick(JournalStickEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        BetonQuest.getInstance().getPlayerData(uuid).getJournal().setStick(e.getID());
    }
}
