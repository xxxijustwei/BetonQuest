package pl.betoncraft.betonquest.api.event;

import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PlayerPointChangeEvent extends JustEvent {

    private final String category;
    private final int count;

    public PlayerPointChangeEvent(Player who, String category, int count) {
        super(who);
        this.category = category;
        this.count = count;
    }
}
