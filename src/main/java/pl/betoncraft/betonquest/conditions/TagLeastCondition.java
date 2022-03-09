package pl.betoncraft.betonquest.conditions;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;

import java.util.UUID;

public class TagLeastCondition extends Condition {

    private final String[] tags;

    public TagLeastCondition(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.tags = instruction.getArray();
    }

    @Override
    public boolean check(UUID uuid) throws QuestRuntimeException {
        PlayerData data = BetonQuest.getInstance().getPlayerData(uuid);
        for (String tag : tags) {
            if (data.hasTag(tag)) return true;
        }

        return false;
    }
}
