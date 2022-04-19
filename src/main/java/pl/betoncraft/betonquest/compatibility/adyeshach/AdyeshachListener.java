package pl.betoncraft.betonquest.compatibility.adyeshach;

import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent;
import ink.ptms.adyeshach.common.entity.EntityInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.conversation.CombatTagger;
import pl.betoncraft.betonquest.utils.MessageUtils;

import java.util.UUID;

@SuppressWarnings("SpellCheckingInspection")
public class AdyeshachListener implements Listener {

    @EventHandler
    public void onInteract(AdyeshachEntityInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (CombatTagger.isTagged(uuid)) {
            MessageUtils.sendNotify(uuid, "busy", "busy,error");
            return;
        }
        EntityInstance entity = event.getEntity();
        int id = Integer.parseInt(entity.getId());
        String assignment = FileManager.getNPC(id);
        if (assignment != null) {
            event.setCancelled(true);
            new AdyeshachConversation(uuid, assignment, entity.getLocation(), entity);
        }
    }
}
