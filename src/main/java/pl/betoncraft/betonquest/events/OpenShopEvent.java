package pl.betoncraft.betonquest.events;

import net.sakuragame.eternal.juststore.api.JustStoreAPI;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

public class OpenShopEvent extends QuestEvent {

    private final String shopID;

    public OpenShopEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        shopID = instruction.next();
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        JustStoreAPI.openShop(PlayerConverter.getPlayer(uuid), shopID);
    }
}
