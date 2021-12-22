package pl.betoncraft.betonquest.events;

import net.sakuragame.eternal.dragoncore.api.ArmourAPI;
import org.bukkit.Bukkit;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ModelClothesEvent extends QuestEvent {

    private final List<String> skins;
    private final Integer duration;

    public ModelClothesEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        String s = instruction.next();
        String d = instruction.getOptional("d");
        this.skins = Arrays.asList(s.split(";"));
        this.duration = (d == null) ? 20 : Integer.parseInt(d);
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        ArmourAPI.setEntitySkin(uuid, skins);

        if (duration <= 0) return;

        Bukkit.getScheduler().runTaskLaterAsynchronously(BetonQuest.getInstance(), () -> ArmourAPI.setEntitySkin(uuid, new ArrayList<>()), duration * 20);
    }
}
