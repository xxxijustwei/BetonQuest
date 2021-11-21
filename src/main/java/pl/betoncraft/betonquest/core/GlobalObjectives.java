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
package pl.betoncraft.betonquest.core;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.core.id.ObjectiveID;

import java.util.*;

/**
 * Handler for global objectives
 *
 * @author Jonas Blocher
 */
public class GlobalObjectives {

    private static GlobalObjectives instance;

    private Set<ObjectiveID> globalObjectives;

    public GlobalObjectives() {
        instance = this;
        globalObjectives = new HashSet<>();
    }

    /**
     * Adds a objective to the list of global objectives.
     * This method should only be called in the constructor of global objectives
     */
    public static void add(ObjectiveID id) {
        instance.globalObjectives.add(id);
    }

    /**
     * Starts all unstarted global objectives for the player
     *
     * @param uuid the id of the player
     */
    public static void startAll(UUID uuid) {
        PlayerData data = BetonQuest.getInstance().getPlayerData(uuid);
        for (ObjectiveID id : instance.globalObjectives) {
            Objective objective = BetonQuest.getQuestManager().getObjective(id);

            if (objective == null) {
                continue;
            }

            //if player already has the tag skip
            if (data.hasTag(GlobalObjectives.getTag(id)))
                continue;
            //start the objective
            objective.newPlayer(uuid);
            //add the tag
            data.addTag(GlobalObjectives.getTag(id));
        }
    }

    /**
     * @param id the id of a global objective
     * @return the tag which marks that the given global objective has already been started for the player
     */
    public static String getTag(ObjectiveID id) {
        return "global-" + id.getBaseID();
    }

    /**
     * @return a list of all loaded global objectives
     */
    public static List<ObjectiveID> list() {
        return new ArrayList<>(instance.globalObjectives);
    }
}
