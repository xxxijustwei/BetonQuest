package pl.betoncraft.betonquest.listener;

import net.citizensnpcs.api.CitizensAPI;
import net.sakuragame.eternal.dragoncore.api.CoreAPI;
import net.sakuragame.eternal.dragoncore.api.KeyPressEvent;
import net.sakuragame.eternal.justmessage.api.event.quest.JournalStickEvent;
import net.sakuragame.eternal.justmessage.screen.ui.quest.ConversationScreen;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.PlayerConversationStartEvent;
import pl.betoncraft.betonquest.conversation.ConversationData;

import java.util.UUID;

public class UIListener implements Listener {

    public UIListener() {
        CoreAPI.registerKey("J");
    }

    @EventHandler
    public void onKeyPress(KeyPressEvent e) {
        Player player = e.getPlayer();
        if (!e.getKey().equals("J")) return;

        UUID uuid = player.getUniqueId();
        BetonQuest.getInstance().getPlayerData(uuid).getJournal().openUI();
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
