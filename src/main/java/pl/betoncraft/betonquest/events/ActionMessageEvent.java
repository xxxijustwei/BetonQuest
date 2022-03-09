package pl.betoncraft.betonquest.events;

import net.sakuragame.eternal.justmessage.api.MessageAPI;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.*;

public class ActionMessageEvent extends QuestEvent {

    private final String[] message;
    private final int delay;

    public ActionMessageEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.message = instruction.getArray();
        this.delay = instruction.getInt(instruction.getOptional("delay"), 1);
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);
        if (message.length == 1) {
            MessageAPI.sendActionTip(player, message[0].replace("~", " "));
            return;
        }

        new BukkitRunnable() {

            private int i = 0;

            @Override
            public void run() {
                if (message.length == i) {
                    cancel();
                    return;
                }
                String msg = message[i];
                MessageAPI.sendActionTip(player, msg.replace("~", " "));
                i++;
            }
        }.runTaskTimerAsynchronously(BetonQuest.getInstance(), 0, delay);
    }
}
