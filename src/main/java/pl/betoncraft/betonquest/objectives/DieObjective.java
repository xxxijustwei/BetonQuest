/*
 * BetonQuest - advanced quests for Bukkit
 * Copyright (C) 2016  Jakub "Co0sh" Sapalski
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.betonquest.objectives;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.LocationData;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Player needs to die. Death can be canceled, also respawn location can be set
 *
 * @author Jakub Sapalski
 */
public class DieObjective extends Objective implements Listener {

    private final boolean cancel;
    private final LocationData location;

    public DieObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = ObjectiveData.class;
        cancel = instruction.hasArgument("cancel");
        location = instruction.getLocation(instruction.getOptional("respawn"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if (cancel) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            UUID uuid = event.getEntity().getUniqueId();
            if (containsPlayer(uuid) && checkConditions(uuid)) {
                completeObjective(uuid);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLastDamage(EntityDamageEvent event) {
        if (!cancel) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID uuid = player.getUniqueId();
            if (containsPlayer(uuid) && player.getHealth() - event.getFinalDamage() <= 0
                    && checkConditions(uuid)) {
                event.setCancelled(true);
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                player.setFoodLevel(20);
                player.setExhaustion(4);
                player.setSaturation(20);
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                if (location != null) {
                    try {
                        player.teleport(location.getLocation(uuid));
                    } catch (QuestRuntimeException e) {
                        LogUtils.getLogger().log(Level.SEVERE, "Couldn't execute onLastDamage in DieObjective");
                        LogUtils.logThrowable(e);
                    }
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setFireTicks(0);

                    }
                }.runTaskLater(BetonQuest.getInstance(), 1);
                completeObjective(uuid);
            }
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
        return "";
    }

}
