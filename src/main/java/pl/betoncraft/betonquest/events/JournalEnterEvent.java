package pl.betoncraft.betonquest.events;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.Journal;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;

import java.util.UUID;

public class JournalEnterEvent extends QuestEvent {

    private final String delJournal;
    private final String addJournal;

    public JournalEnterEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.delJournal = instruction.next();
        this.addJournal = instruction.next();
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Journal journal = BetonQuest.getInstance().getPlayerData(uuid).getJournal();
        journal.removePointer(delJournal);
        journal.addPointer(addJournal);
        journal.setStick(addJournal);
        journal.update();
    }
}
