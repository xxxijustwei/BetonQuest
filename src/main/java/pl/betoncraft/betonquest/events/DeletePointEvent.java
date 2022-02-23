package pl.betoncraft.betonquest.events;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;

import java.util.UUID;

public class DeletePointEvent extends QuestEvent {

    protected final String category;

    public DeletePointEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        persistent = true;
        staticness = true;
        category = instruction.next();
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        PlayerData playerData = BetonQuest.getInstance().getPlayerData(uuid);
        playerData.removePoints(category);
    }
}