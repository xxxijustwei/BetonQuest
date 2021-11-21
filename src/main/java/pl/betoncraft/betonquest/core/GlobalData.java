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
import pl.betoncraft.betonquest.core.Point;
import pl.betoncraft.betonquest.utils.Scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object storing all player-related data, which can load and save it.
 *
 * @author Jonas Blocher
 */
public class GlobalData {

    private List<String> global_tags = new ArrayList<>();
    private List<Point> global_points = new ArrayList<>();

    public GlobalData() {
        loadAllGlobalData();
    }

    /**
     * Loads all data for the player and puts it in appropriate lists.
     */
    public void loadAllGlobalData() {
        global_tags = BetonQuest.getStorageManager().getGlobalTags();
        global_points = BetonQuest.getStorageManager().getGlobalPoints();
    }

    /**
     * Returns the List of Tags
     *
     * @return the List of Tags
     */
    public List<String> getTags() {
        return global_tags;
    }

    /**
     * Checks if the there is a global tag set
     *
     * @param tag tag to check
     * @return true if the tag is set
     */
    public boolean hasTag(String tag) {
        return global_tags.contains(tag);
    }

    /**
     * Adds the specified tag to global list. It won't double it however.
     *
     * @param tag tag to add
     */
    public void addTag(String tag) {
        if (!global_tags.contains(tag)) {
            global_tags.add(tag);
            Scheduler.runAsync(() -> BetonQuest.getStorageManager().insertGlobalTag(tag));
        }
    }

    /**
     * Removes the specified tag from global list. If there is no tag, nothing
     * happens.
     *
     * @param tag tag to remove
     */
    public void removeTag(String tag) {
        global_tags.remove(tag);
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().deleteGlobalTag(tag));
    }

    /**
     * Returns the List of Points.
     *
     * @return the List of Points
     */
    public List<Point> getPoints() {
        return global_points;
    }

    /**
     * Returns the amount of point the in specified category. If the
     * category does not exist, it will return 0.
     *
     * @param category name of the category
     * @return amount of global_points
     */
    public int hasPointsFromCategory(String category) {
        for (Point p : global_points) {
            if (p.getCategory().equals(category)) {
                return p.getCount();
            }
        }
        return 0;
    }

    /**
     * Adds or subtracts global_points to/from specified category. If there is no such category it will
     * be created.
     *
     * @param category global_points will be added to this category
     * @param count    how much global_points will be added (or subtracted if negative)
     */
    public void modifyPoints(String category, int count) {
        for (Point point : global_points) {
            if (point.getCategory().equalsIgnoreCase(category)) {
                Scheduler.runAsync(() -> BetonQuest.getStorageManager().updateGlobalPoints(category, point.getCount() + count));
                point.addPoints(count);
                return;
            }
        }

        global_points.add(new Point(category, count));
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().insertGlobalPoints(category, count));
    }

    /**
     * Removes the whole category of global_points.
     *
     * @param category name of a point category
     */
    public void removePointsCategory(String category) {
        Point pointToRemove = null;
        for (Point point : global_points) {
            if (point.getCategory().equalsIgnoreCase(category)) {
                pointToRemove = point;
            }
        }
        if (pointToRemove != null) {
            global_points.remove(pointToRemove);
        }
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().deleteGlobalPoints(category));
    }

    /**
     * Purges all global data from the database and from this object.
     */
    public void purge() {
        global_tags.clear();
        global_points.clear();
        Scheduler.runAsync(() -> {
            BetonQuest.getStorageManager().deleteGlobalTags();
            BetonQuest.getStorageManager().deleteGlobalPoints();
        });
    }

    /**
     * Purges all global tags from the database and from this object
     */
    public void purgeTags() {
        // clear all lists
        global_tags.clear();
        // clear the database
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().deleteGlobalTags());
    }

    /**
     * Purges all global points from the database and from this object
     */
    public void purgePoints() {
        // clear all lists
        global_points.clear();
        // clear the database
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().deleteGlobalPoints());
    }
}
