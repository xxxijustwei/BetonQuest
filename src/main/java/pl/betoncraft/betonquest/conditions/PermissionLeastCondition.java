package pl.betoncraft.betonquest.conditions;

import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

public class PermissionLeastCondition extends Condition {

    private final String[] permissions;

    public PermissionLeastCondition(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.permissions = instruction.getArray();
    }

    @Override
    public boolean check(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);
        for (String perm : permissions) {
            if (player.hasPermission(perm)) return true;
        }
        return false;
    }
}
