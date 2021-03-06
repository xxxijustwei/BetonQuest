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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.BlockSelector;
import pl.betoncraft.betonquest.utils.LocationData;
import pl.betoncraft.betonquest.utils.LogUtils;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Player has to click on block (or air). Left click, right click and any one of
 * them is supported.
 *
 * @author Jakub Sapalski
 */
public class ActionObjective extends Objective implements Listener {

    private Click action;
    private BlockSelector selector;
    private LocationData loc;
    private VariableNumber range;
    private boolean cancel = false;

    public ActionObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = ObjectiveData.class;
        action = instruction.getEnum(Click.class);
        if (instruction.next().equalsIgnoreCase("any")) {
            selector = null;
        } else {
            selector = instruction.getBlockSelector(instruction.current());
        }
        loc = instruction.getLocation(instruction.getOptional("loc"));
        String r = instruction.getOptional("range");
        range = instruction.getVarNum(r == null ? "1" : r);
        cancel = instruction.hasArgument("cancel");

        if (selector != null && !selector.isValid()) {
            throw new InstructionParseException("Invalid selector: " + selector.toString());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        // Only fire the event for the main hand to avoid that the event is triggered two times.
        if (event.getHand() == EquipmentSlot.OFF_HAND && event.getHand() != null) {
            return; // off hand packet, ignore.
        }
        UUID uuid = event.getPlayer().getUniqueId();
        if (!containsPlayer(uuid)) {
            return;
        }
        if (selector == null) {
            switch (action) {
                case RIGHT:
                    if ((event.getAction().equals(Action.RIGHT_CLICK_AIR)
                            || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && checkConditions(uuid)) {
                        if (cancel)
                            event.setCancelled(true);
                        completeObjective(uuid);
                    }
                    break;
                case LEFT:
                    if ((event.getAction().equals(Action.LEFT_CLICK_AIR)
                            || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) && checkConditions(uuid)) {
                        if (cancel)
                            event.setCancelled(true);
                        completeObjective(uuid);
                    }
                    break;
                case ANY:
                default:
                    if ((event.getAction().equals(Action.LEFT_CLICK_AIR)
                            || event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                            || event.getAction().equals(Action.RIGHT_CLICK_AIR)
                            || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && checkConditions(uuid)) {
                        if (cancel)
                            event.setCancelled(true);
                        completeObjective(uuid);
                    }
                    break;
            }
        } else {
            Action actionEnum;
            switch (action) {
                case RIGHT:
                    actionEnum = Action.RIGHT_CLICK_BLOCK;
                    break;
                case LEFT:
                    actionEnum = Action.LEFT_CLICK_BLOCK;
                    break;
                case ANY:
                default:
                    actionEnum = null;
                    break;
            }
            try {
                if (((actionEnum == null && (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                        || event.getAction().equals(Action.LEFT_CLICK_BLOCK))) || event.getAction().equals(actionEnum))
                        && (event.getClickedBlock() != null && (((selector.match(Material.FIRE) || selector.match(Material.LAVA) || selector.match(Material.WATER))
                        && selector.match(event.getClickedBlock().getRelative(event.getBlockFace())))
                        || selector.match(event.getClickedBlock())))) {
                    if (loc != null) {
                        Location location = loc.getLocation(uuid);
                        double r = range.getDouble(uuid);
                        if (!event.getClickedBlock().getWorld().equals(location.getWorld())
                                || event.getClickedBlock().getLocation().distance(location) > r) {
                            return;
                        }
                    }
                    if (checkConditions(uuid)) {
                        if (cancel) {
                            event.setCancelled(true);
                        }
                        completeObjective(uuid);
                    }
                }
            } catch (QuestRuntimeException e) {
                LogUtils.getLogger().log(Level.WARNING, "Error while handling '" + instruction.getID() + "' objective: " + e.getMessage());
                LogUtils.logThrowable(e);
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

    @Override
    public String getProperty(String name, UUID uuid) {
        if (name.equalsIgnoreCase("location")) {
            if (loc == null) {
                return "";
            }
            Location location;
            try {
                location = loc.getLocation(uuid);
            } catch (QuestRuntimeException e) {
                LogUtils.getLogger().log(Level.WARNING, "Error while getting location property in '" + instruction.getID() + "' objective: "
                        + e.getMessage());
                LogUtils.logThrowable(e);
                return "";
            }
            return "X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ();
        }
        return "";
    }

    public enum Click {
        RIGHT, LEFT, ANY
    }

}
