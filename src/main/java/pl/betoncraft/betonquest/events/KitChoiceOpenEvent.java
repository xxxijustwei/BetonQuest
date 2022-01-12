package pl.betoncraft.betonquest.events;

import net.sakuragame.eternal.justkit.api.JustKitAPI;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

public class KitChoiceOpenEvent extends QuestEvent {

    private final String kitID;
    private final String option;

    public KitChoiceOpenEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.kitID = instruction.next();
        this.option = instruction.next();
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);
        JustKitAPI.openChoiceKit(player, kitID, option);
    }
}
