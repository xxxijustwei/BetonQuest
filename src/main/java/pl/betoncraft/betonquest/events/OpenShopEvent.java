package pl.betoncraft.betonquest.events;

import net.sakuragame.eternal.justshop.api.JustShopAPI;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

public class OpenShopEvent extends QuestEvent {

    private final String shopID;
    private final int category;

    public OpenShopEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        shopID = instruction.next();
        String part = instruction.next();
        category = (part == null) ? 1 : Integer.parseInt(part);
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        JustShopAPI.open(PlayerConverter.getPlayer(uuid), shopID, category);
    }
}
