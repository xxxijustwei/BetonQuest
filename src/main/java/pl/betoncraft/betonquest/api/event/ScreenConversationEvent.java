package pl.betoncraft.betonquest.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Setter
@Getter
public class ScreenConversationEvent extends PlayerEvent {

    private final String id;
    private String npcName;
    private String response;

    private final static HandlerList handlerList = new HandlerList();

    public ScreenConversationEvent(Player who, String id, String npcName, String response) {
        super(who);
        this.id = id;
        this.npcName = npcName;
        this.response = response;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public void call() {
        Bukkit.getPluginManager().callEvent(this);
    }
}
