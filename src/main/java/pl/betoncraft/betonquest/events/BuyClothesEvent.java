package pl.betoncraft.betonquest.events;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.clothes.Merchant;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;

import java.util.UUID;

public class BuyClothesEvent extends QuestEvent {

    public BuyClothesEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Merchant merchant = BetonQuest.getClothesManager().getCurrentMerchant(uuid);
        if (merchant == null) return;

        // TODO
    }
}
