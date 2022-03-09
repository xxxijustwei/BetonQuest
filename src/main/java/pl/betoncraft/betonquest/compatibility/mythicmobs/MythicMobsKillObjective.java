package pl.betoncraft.betonquest.compatibility.mythicmobs;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;

import java.util.*;

public class MythicMobsKillObjective extends Objective implements Listener {

    private final Map<String, Integer> goals;
    private final String[] mobs;

    public MythicMobsKillObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.template = KillData.class;
        this.goals = new HashMap<>();
        this.mobs = instruction.getArray();

        for (String s : mobs) {
            String[] args = s.split(":");
            String id = args[0];
            int count = Integer.parseInt(args[1]);
            this.goals.put(id, count);
        }
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
        return String.join(",", mobs);
    }

    @Override
    public String getProperty(String name, UUID uuid) {
        String[] args = name.split(":");
        String id = args[0];
        String type = args[1];

        KillData data = (KillData) this.dataMap.get(uuid);
        if (type.equalsIgnoreCase("left")) {
            return "" + data.getSurplus(id);
        }

        if (type.equalsIgnoreCase("amount")) {
            return "" + (this.goals.get(id) - data.getSurplus(id));
        }

        return "";
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(MythicMobDeathEvent e) {
        if (!(e.getKiller() instanceof Player)) return;

        Player player = (Player) e.getKiller();
        UUID uuid = player.getUniqueId();
        MythicMob mob = e.getMobType();

        if (!goals.containsKey(mob.getInternalName())) return;
        if (!(containsPlayer(uuid) && checkConditions(uuid))) return;

        KillData data = (KillData) this.dataMap.get(uuid);
        data.kill(mob.getInternalName());
        if (!data.isFinished()) return;

        completeObjective(uuid);
    }

    public static class KillData extends ObjectiveData {

        private final Map<String, Integer> goals;

        public KillData(String instruction, UUID uuid, String objID) {
            super(instruction, uuid, objID);
            this.goals = new HashMap<>();
            String[] mobs = instruction.split(",");
            for (String mob : mobs) {
                String[] args = mob.split(":");
                String id = args[0];
                int count = Integer.parseInt(args[1]);
                this.goals.put(id, count);
            }
        }

        private int getSurplus(String id) {
            return this.goals.get(id);
        }

        private void kill(String id) {
            this.goals.computeIfPresent(id, (key, value) -> {
                int surplus = value - 1;
                return Math.max(surplus, 0);
            });
            update();
        }

        private boolean isFinished() {
            for (int count : this.goals.values()) {
                if (count > 0) return false;
            }

            return true;
        }

        @Override
        public String toString() {
            List<String> data = new ArrayList<>();
            this.goals.forEach((key, value) -> data.add(key + ":" + value));
            return String.join(",", data);
        }
    }
}
