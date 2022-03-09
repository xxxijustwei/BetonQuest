package pl.betoncraft.betonquest.events;

import net.sakuragame.eternal.waypoints.api.WaypointsAPI;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class WaypointEvent extends QuestEvent {

    private final String type;
    private final String id;

    public WaypointEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.type = instruction.next();
        this.id = instruction.next();
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);

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
