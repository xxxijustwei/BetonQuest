package pl.betoncraft.betonquest.compatibility.adyeshach;

import ink.ptms.adyeshach.common.entity.EntityInstance;
import lombok.Getter;
import org.bukkit.Location;
import pl.betoncraft.betonquest.conversation.Conversation;

import java.util.UUID;

@SuppressWarnings("SpellCheckingInspection")
public class AdyeshachConversation extends Conversation {

    @Getter
    private final EntityInstance npc;

    public AdyeshachConversation(UUID uuid, String conversationID, Location location, EntityInstance npc) {
        super(uuid, conversationID, location);
        this.npc = npc;
    }
}
