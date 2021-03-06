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
package pl.betoncraft.betonquest.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Should be fired when the quest data updates
 *
 * @author Jakub Sapalski
 */
public class QuestDataUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private UUID uuid;
    private String objID;
    private String data;

    public QuestDataUpdateEvent(UUID uuid, String objID, String data) {
        this.uuid = uuid;
        this.objID = objID;
        this.data = data;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getObjID() {
        return objID;
    }

    public String getData() {
        return data;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
