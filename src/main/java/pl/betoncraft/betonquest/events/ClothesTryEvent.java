package pl.betoncraft.betonquest.events;

import net.sakuragame.eternal.dragoncore.api.ArmourAPI;
import org.bukkit.Bukkit;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.clothes.Merchant;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClothesTryEvent extends QuestEvent {

    private final String skin;
    private final Integer duration;

    public ClothesTryEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        String s = instruction.next();
        String d = instruction.getOptional("d");
        this.skin = s;
        this.duration = (d == null) ? 20 : Integer.parseInt(d);
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        List<String> skins = skin.equals("merchant") ? getMerchantSkins(uuid) : Arrays.asList(skin.split(";"));

        ArmourAPI.setEntitySkin(uuid, skins);

        if (duration <= 0) return;

        Bukkit.getScheduler().runTaskLaterAsynchronously(BetonQuest.getInstance(), () -> ArmourAPI.setEntitySkin(uuid, new ArrayList<>()), duration * 20);
    }

    private List<String> getMerchantSkins(UUID uuid) {
        Merchant merchant = BetonQuest.getClothesManager().getDialogueMerchant(uuid);
        if (merchant == null) return new ArrayList<>();

        return merchant.getSkins();
    }
}
