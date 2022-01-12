package pl.betoncraft.betonquest.listener;

import net.citizensnpcs.api.CitizensAPI;
import net.sakuragame.eternal.justmessage.screen.ui.conversation.ConversationScreen;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.api.event.PlayerConversationStartEvent;
import pl.betoncraft.betonquest.conversation.ConversationData;

public class ConversationListener implements Listener {

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
}
