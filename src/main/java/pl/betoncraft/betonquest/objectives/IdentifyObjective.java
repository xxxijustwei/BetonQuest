package pl.betoncraft.betonquest.objectives;

import com.sakuragame.eternal.justattribute.api.event.smithy.SmithyIdentifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;

import java.util.UUID;

public class IdentifyObjective extends Objective implements Listener {

    private final int count;

    public IdentifyObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.template = IdentifyData.class;
        this.count = instruction.getInt();
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getDefaultDataInstruction() {
        return "" + count;
    }

    @Override
    public String getProperty(String name, UUID uuid) {
        if (name.equalsIgnoreCase("left")) {
            return Integer.toString(((IdentifyData) dataMap.get(uuid)).getAmount());
        } else if (name.equalsIgnoreCase("amount")) {
            return Integer.toString(count - ((IdentifyData) dataMap.get(uuid)).getAmount());
        }
        return "";
    }

    @EventHandler
    public void onIdentify(SmithyIdentifyEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!this.containsPlayer(uuid)) return;

        IdentifyData data = (IdentifyData) this.dataMap.get(uuid);
        data.minus();
        if (!data.isFinished()) return;

        completeObjective(uuid);
    }

    public static class IdentifyData extends ObjectiveData {

        private int amount;

        public IdentifyData(String instruction, UUID uuid, String objID) {
            super(instruction, uuid, objID);
            this.amount = Integer.parseInt(instruction);
        }

        public int getAmount() {
            return amount;
        }

        public void minus() {
            this.amount--;
            this.update();
        }

        public boolean isFinished() {
            return this.amount <= 0;
        }

        @Override
        public String toString() {
            return "" + amount;
        }
    }
}
