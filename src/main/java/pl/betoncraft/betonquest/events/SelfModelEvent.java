package pl.betoncraft.betonquest.events;

import net.sakuragame.eternal.justmessage.screen.ui.conversation.ConversationScreen;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

public class SelfModelEvent extends QuestEvent {

    private final double scale;

    public SelfModelEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.scale = Double.parseDouble(instruction.next());
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        ConversationScreen.setConvNPC(PlayerConverter.getPlayer(uuid), uuid, scale);
    }
}
