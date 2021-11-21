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
package pl.betoncraft.betonquest.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.conversation.CombatTagger;
import pl.betoncraft.betonquest.conversation.Conversation;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.MessageUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Listener which starts conversation on clicking on NPCs made from blocks.
 *
 * @author Jakub Sapalski
 */
public class CubeNPCListener implements Listener {

    /**
     * This limits NPC creation by canceling all sign edits where first line is
     * "[NPC]"
     *
     * @param event SignChangeEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onSignPlace(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase("[NPC]") && !event.getPlayer().hasPermission("betonquest.admin")) {
            // if the player doesn't have the required permission deny the
            // editing
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer().getUniqueId(), "no_permission");
        }
    }

    /**
     * This checks if the player clicked on valid NPC, and starts the
     * conversation
     *
     * @param event PlayerInteractEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onNPCClick(PlayerInteractEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // check if the player has required permission
        if (!event.getPlayer().hasPermission("betonquest.conversation")) {
            return;
        }
        // check if the blocks are placed in the correct way
        String conversationID = null;
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && event.getClickedBlock().getType().equals(Material.SKULL)) {
            Block block = event.getClickedBlock().getLocation().clone().add(0, -1, 0).getBlock();
            if (block.getType().equals(Material.STAINED_CLAY)) {
                Block[] signs = new Block[]{block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST),
                        block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH)};
                Sign theSign = null;
                byte count = 0;
                for (Block sign : signs) {
                    if (sign.getType().equals(Material.WALL_SIGN) && sign.getState() instanceof Sign) {
                        theSign = (Sign) sign.getState();
                        count++;
                    }
                }
                if (count == 1 && theSign != null && theSign.getLine(0).equalsIgnoreCase("[NPC]")) {
                    conversationID = theSign.getLine(1);
                }
            }

        }
        // if the conversation ID was extracted from NPC then start the
        // conversation
        if (conversationID != null) {
            String assignment = FileManager.getNPC(Integer.parseInt(conversationID));
            if (assignment != null) {
                if (CombatTagger.isTagged(uuid)) {
                    MessageUtils.sendMessage(uuid, "busy");
                    return;
                }
                event.setCancelled(true);
                new Conversation(uuid, assignment, event.getClickedBlock().getLocation().add(0.5, -1, 0.5));
            } else {
                LogUtils.getLogger().log(Level.WARNING, "Cannot start conversation: nothing assigned to " + conversationID);
            }
        }
    }
}
