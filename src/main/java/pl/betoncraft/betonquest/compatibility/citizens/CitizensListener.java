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
package pl.betoncraft.betonquest.compatibility.citizens;

import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.conversation.CombatTagger;
import pl.betoncraft.betonquest.utils.MessageUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

/**
 * Starts new conversations with NPCs
 *
 * @author Jakub Sapalski
 */
public class CitizensListener implements Listener {

    /**
     * Initializes the listener
     */
    public CitizensListener() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNPCClick(final NPCRightClickEvent event) {
        if (event.isCancelled()) return;

        if (NPCMoveEvent.blocksTalking(event.getNPC())) {
            return;
        }
        UUID uuid = event.getClicker().getUniqueId();
        if (CombatTagger.isTagged(uuid)) {
            MessageUtils.sendNotify(uuid, "busy", "busy,error");
            return;
        }
        int id = event.getNPC().getId();
        String assignment = FileManager.getNPC(id);
        if (assignment != null) {
            event.setCancelled(true);
            new CitizensConversation(uuid, assignment, event.getNPC().getEntity().getLocation(), event.getNPC());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void CitizensReload(final CitizensReloadEvent event) {
        CitizensHologram.reload();
    }
}
