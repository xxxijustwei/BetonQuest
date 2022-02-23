package pl.betoncraft.betonquest.events;

import eos.moe.armourers.api.DragonAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.clothes.Merchant;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

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
        Player player = PlayerConverter.getPlayer(uuid);

        if (skin.equals("recover")) {
            DragonAPI.updatePlayerSkin(player);
            Integer tid = BetonQuest.getClothesManager().getTryMap().get(uuid);
            if (tid != null) {
                Bukkit.getScheduler().cancelTask(tid);
            }
            return;
        }

        List<String> skins = skin.equals("merchant") ? getMerchantSkins(uuid) : Arrays.asList(skin.split(";"));

        DragonAPI.setEntitySkin(uuid, skins);

        if (duration <= 0) return;

        Integer tid = BetonQuest.getClothesManager().getTryMap().get(uuid);
        if (tid != null) {
            Bukkit.getScheduler().cancelTask(tid);
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                DragonAPI.updatePlayerSkin(player);
            }
        }.runTaskLaterAsynchronously(BetonQuest.getInstance(), duration * 20);
        BetonQuest.getClothesManager().getTryMap().put(uuid, task.getTaskId());
    }

    private List<String> getMerchantSkins(UUID uuid) {
        Merchant merchant = BetonQuest.getClothesManager().getDialogueMerchant(uuid);
        if (merchant == null) return new ArrayList<>();

        return merchant.getSkins();
    }
}
