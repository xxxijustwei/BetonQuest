package pl.betoncraft.betonquest.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class QuestAccountLoadedEvent extends PlayerEvent {

    private final static HandlerList HANDLER_LIST = new HandlerList();

    public QuestAccountLoadedEvent(Player player) {
        super(player);
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
