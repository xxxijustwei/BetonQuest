package pl.betoncraft.betonquest.events;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

public class ChatEvent extends QuestEvent {

    private final String message;

    public ChatEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.message = instruction.getInstruction().split(" ", 2)[1];
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);
        player.sendMessage(MegumiUtil.onReplace(message));
    }
}
