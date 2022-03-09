package pl.betoncraft.betonquest.objectives;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.api.event.PlayerPointChangeEvent;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;

public class PointChangeObjective extends Objective implements Listener {

    private final String category;
    private final int count;

    public PointChangeObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.template = ObjectiveData.class;
        this.category = instruction.next();
        this.count = instruction.getInt();
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getDefaultDataInstruction() {
        return String.valueOf(count);
    }

    @EventHandler
    public void onChange(PlayerPointChangeEvent e) {
        Player player = e.getPlayer();
        String category = e.getCategory();
        int count = e.getCount();

        if (!containsPlayer(player.getUniqueId())) return;
        if (!this.category.equals(category)) return;
        if (this.count != count) return;

        completeObjective(player.getUniqueId());
    }
}
