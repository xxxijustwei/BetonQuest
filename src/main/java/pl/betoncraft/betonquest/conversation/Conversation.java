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
package pl.betoncraft.betonquest.conversation;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.api.event.ConversationOptionEvent;
import pl.betoncraft.betonquest.api.event.PlayerConversationEndEvent;
import pl.betoncraft.betonquest.api.event.PlayerConversationStartEvent;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.conversation.ConversationData.OptionType;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.core.id.EventID;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.MessageUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Represents a conversation between player and NPC
 *
 * @author Jakub Sapalski
 */
public class Conversation implements Listener {

    private static ConcurrentHashMap<UUID, Conversation> list = new ConcurrentHashMap<>();

    private final UUID uuid;
    private final Player player;
    private final ConfigPackage pack;
    private final Location location;
    private final String convID;
    private final List<String> blacklist;
    private final Conversation conv;
    private final BetonQuest plugin;
    private ConversationData data;
    private ConversationIO inOut;
    private String option;
    private boolean ended = false;
    private boolean messagesDelaying = false;
    private Interceptor interceptor;

    private HashMap<Integer, String> current = new HashMap<>();


    /**
     * Starts a new conversation between player and npc at given location. It uses
     * starting options to determine where to start.
     *
     * @param uuid           ID of the player
     * @param conversationID ID of the conversation
     * @param location       location where the conversation has been started
     */
    public Conversation(UUID uuid, String conversationID, Location location) {
        this(uuid, conversationID, location, null);
    }

    /**
     * Starts a new conversation between player and npc at given location,
     * starting with the given option. If the option is null, then it will start
     * from the beginning.
     *
     * @param uuid           ID of the player
     * @param conversationID ID of the conversation
     * @param location       location where the conversation has been started
     * @param option         ID of the option from where to start
     */
    public Conversation(UUID uuid, final String conversationID,
                        Location location, String option) {

        this.conv = this;
        this.plugin = BetonQuest.getInstance();
        this.uuid = uuid;
        this.player = PlayerConverter.getPlayer(uuid);
        this.pack = FileManager.getPackages();
        this.location = location;
        this.convID = conversationID;
        this.data = BetonQuest.getQuestManager().getConversation(convID);
        this.blacklist = plugin.getConfig().getStringList("cmd_blacklist");
        this.messagesDelaying = plugin.getConfig().getString("display_chat_after_conversation").equalsIgnoreCase("true");

        // check if data is present
        if (data == null) {
            LogUtils.getLogger().log(Level.WARNING, "Conversation doesn't exist: " + conversationID);
            return;
        }

        // if the player has active conversation, terminate this one
        if (list.containsKey(uuid)) {
            LogUtils.getLogger().log(Level.FINE, "Player " + PlayerConverter.getName(uuid) + " is in conversation right now, returning.");
            return;
        }

        // add the player to the list of active conversations
        list.put(uuid, conv);

        String[] options;
        if (option == null) {
            options = null;
        } else {
            if (!option.contains("."))
                option = conversationID.substring(conversationID.indexOf('.') + 1) + "." + option;
            options = new String[]{option};
        }

        new Starter(options).runTaskAsynchronously(BetonQuest.getInstance());
    }

    /**
     * Checks if the player is in a conversation
     *
     * @param uuid ID of the player
     * @return if the player is on the list of active conversations
     */
    public static boolean containsPlayer(UUID uuid) {
        return list.containsKey(uuid);
    }

    /**
     * Gets this player's active conversation.
     *
     * @param uuid ID of the player
     * @return player's active conversation or null if there is no conversation
     */
    public static Conversation getConversation(UUID uuid) {
        return list.get(uuid);
    }

    /**
     * Chooses the first available option.
     *
     * @param options list of option pointers separated by commas
     * @param force   setting it to true will force the first option, even if
     *                conditions are not met
     */
    private void selectOption(String[] options, boolean force) {

        if (force) {
            options = new String[]{options[0]};
        }
        // get npc's text
        option = null;
        options:
        for (String option : options) {
            String convName, optionName;
            if (option.contains(".")) {
                String[] parts = option.split("\\.");
                convName = parts[0];
                optionName = parts[1];
            } else {
                convName = data.getName();
                optionName = option;
            }
            ConversationData currentData = BetonQuest.getQuestManager().getConversation(convName);
            if (!force)
                for (ConditionID condition : currentData.getConditionIDs(optionName, OptionType.NPC)) {
                    if (!QuestManager.condition(this.uuid, condition)) {
                        continue options;
                    }
                }
            this.option = optionName;
            data = currentData;
            break;
        }
    }

    /**
     * Sends to the player the text said by NPC. It uses the selected option and
     * displays it. Note: this method now requires a prior call to
     * selectOption()
     */
    private void printNPCText() {

        // if there are no possible options, end conversation
        if (option == null) {
            new ConversationEnder().runTask(BetonQuest.getInstance());
            return;
        }
        String text = data.getText(uuid, option, OptionType.NPC);
        // resolve variables
        for (String variable : QuestManager.resolveVariables(text)) {
            text = text.replace(variable, BetonQuest.getQuestManager().getVariableValue(variable, uuid));
        }
        // print option to the player
        inOut.setNpcResponse(data.getQuester(), text);

        new NPCEventRunner(option).runTask(BetonQuest.getInstance());
    }

    /**
     * Passes given string as answer from player in a conversation.
     *
     * @param number the message player has sent on chat
     */
    public void passPlayerAnswer(int number) {
        inOut.clear();

        String option = current.get(number);
        new PlayerEventRunner(option).runTask(BetonQuest.getInstance());

        // clear hashmap
        current.clear();
    }

    /**
     * Prints answers the player can choose.
     *
     * @param options list of pointers to player options separated by commas
     */
    private void printOptions(String[] options) {
        // i is for counting replies, like 1. something, 2. something else
        int i = 0;
        answers:
        for (String option : options) {
            for (ConditionID condition : data.getConditionIDs(option, OptionType.PLAYER)) {
                if (!QuestManager.condition(uuid, condition)) {
                    continue answers;
                }
            }
            i++;
            // print reply and put it to the hashmap
            current.put(i, option);
            // replace variables with their values
            String text = data.getText(uuid, option, OptionType.PLAYER);
            for (String variable : QuestManager.resolveVariables(text)) {
                text = text.replace(variable, BetonQuest.getQuestManager().getVariableValue(variable, uuid));
            }
            inOut.addPlayerOption(text);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                inOut.display();
            }
        }.runTask(BetonQuest.getInstance());
        // end conversations if there are no possible options
        if (current.isEmpty()) {
            new ConversationEnder().runTask(BetonQuest.getInstance());
        }
    }

    /**
     * Ends conversation, firing final events and removing it from the list of
     * active conversations
     */
    public void endConversation() {
        if (ended)
            return;
        ended = true;
        inOut.end();
        // fire final events
        for (EventID event : data.getFinalEvents()) {
            QuestManager.event(uuid, event);
        }
        //only display status messages if conversationIO allows it
        if (conv.inOut.printMessages()) {
            // print message
            conv.inOut.print(MessageUtils.parseMessage(uuid, "conversation_end", new String[]{data.getQuester()}));
        }
        //play conversation end sound
        MessageUtils.playSound(uuid, "end");

        // End interceptor after a second
        if (interceptor != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    interceptor.end();
                }
            }.runTaskLater(BetonQuest.getInstance(), 20);
        }

        // delete conversation
        list.remove(uuid);
        HandlerList.unregisterAll(this);

        new BukkitRunnable() {

            @Override
            public void run() {
                Bukkit.getServer().getPluginManager().callEvent(new PlayerConversationEndEvent(player, Conversation.this));
            }
        }.runTask(BetonQuest.getInstance());
    }

    /**
     * @return whenever this conversation has already ended
     */
    public boolean isEnded() {
        return ended;
    }

    /**
     * Send message to player, bypassing any message delaying if needed
     */
    public void sendMessage(String message) {
        if (interceptor != null) {
            interceptor.sendMessage(message);
        } else {
            player.spigot().sendMessage(TextComponent.fromLegacyText(message));
        }
    }

    public void sendMessage(BaseComponent[] message) {
        if (interceptor != null) {
            interceptor.sendMessage(message);
        } else {
            player.spigot().sendMessage(message);
        }
    }

    /**
     * Checks if the movement of the player should be blocked.
     *
     * @return true if the movement should be blocked, false otherwise
     */
    public boolean isMovementBlock() {
        return data.isMovementBlocked();
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().equals(player)) {
            return;
        }
        if (event.getMessage() == null)
            return;
        String cmdName = event.getMessage().split(" ")[0].substring(1);
        if (blacklist.contains(cmdName)) {
            event.setCancelled(true);

            MessageUtils.sendNotify(event.getPlayer().getUniqueId(), "command_blocked", "command_blocked,error");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        // prevent damage to (or from) player while in conversation
        if (!(event.getDamager() instanceof Player)) return;
        Player vitim = (Player) event.getDamager();
        if (!vitim.equals(player)) return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        // if player quits, end conversation (why keep listeners running?)
        if (event.getPlayer().equals(player)) {
            endConversation();
        }
    }

    /**
     * @return the location where the conversation has been started
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return the ConversationIO object used by this conversation
     */
    public ConversationIO getIO() {
        return inOut;
    }

    /**
     * @return the data of the conversation
     */
    public ConversationData getData() {
        return data;
    }

    /**
     * @return the package containing this conversation
     */
    public ConfigPackage getPackage() {
        return pack;
    }

    /**
     * @return the ID of the conversation
     */
    public String getID() {
        return convID;
    }

    /**
     * Starts the conversation, should be called asynchronously.
     *
     * @author Jakub Sapalski
     */
    private class Starter extends BukkitRunnable {

        private String[] options;

        public Starter(String[] options) {
            this.options = options;
        }

        public void run() {
            // the conversation start event must be run on next tick
            PlayerConversationStartEvent event = new PlayerConversationStartEvent(player, conv);
            new BukkitRunnable() {

                @Override
                public void run() {
                    Bukkit.getServer().getPluginManager().callEvent(event);
                }
            }.runTask(BetonQuest.getInstance());

            // stop the conversation if it's canceled
            if (event.isCancelled())
                return;

            // now the conversation should start no matter what;
            // the inOut can be safely instantiated; doing it before
            // would leave it active while the conversation is not
            // started, causing it to display "null" all the time
            try {
                String name = data.getConversationIO();
                Class<? extends ConversationIO> c = QuestManager.getConvIO(name);
                conv.inOut = c.getConstructor(Conversation.class, UUID.class).newInstance(conv, uuid);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LogUtils.getLogger().log(Level.WARNING, "Error when loading conversation IO");
                LogUtils.logThrowable(e);
                return;
            }

            // register listener for immunity and blocking commands
            Bukkit.getPluginManager().registerEvents(conv, BetonQuest.getInstance());

            // start interceptor if needed
            if (messagesDelaying) {
                try {
                    String name = data.getInterceptor();
                    Class<? extends Interceptor> c = QuestManager.getInterceptor(name);
                    conv.interceptor = c.getConstructor(Conversation.class, UUID.class).newInstance(conv, uuid);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    LogUtils.getLogger().log(Level.WARNING, "Error when loading interceptor");
                    LogUtils.logThrowable(e);
                    return;
                }
            }

            if (options == null) {
                options = data.getStartingOptions();

                // first select the option before sending message, so it
                // knows which is used
                selectOption(options, false);

                // check whether to add a
                String prefixName = null;
                String[] prefixVariables = null;

                //only display status messages if conversationIO allows it
                if (conv.inOut.printMessages()) {
                    // print message about starting a conversation only if it
                    // is started, not resumed
                    conv.inOut.print(MessageUtils.parseMessage(uuid, "conversation_start", new String[]{data.getQuester()},
                            prefixName, prefixVariables));
                }
                //play the conversation start sound
                MessageUtils.playSound(uuid, "start");
            } else {
                // don't forget to select the option prior to printing its text
                selectOption(options, true);
            }

            // print NPC's text
            printNPCText();
            ConversationOptionEvent e = new ConversationOptionEvent(player, conv, option, conv.option);

            new BukkitRunnable() {

                @Override
                public void run() {
                    Bukkit.getPluginManager().callEvent(e);
                }
            }.runTask(BetonQuest.getInstance());

        }
    }

    /**
     * Fires events from the option. Should be called in the main thread.
     *
     * @author Jakub Sapalski
     */
    private class NPCEventRunner extends BukkitRunnable {

        private String option;

        public NPCEventRunner(String option) {
            this.option = option;
        }

        public void run() {
            new OptionPrinter(option).runTaskAsynchronously(BetonQuest.getInstance());
        }
    }

    /**
     * Fires events from the option. Should be called in the main thread.
     *
     * @author Jakub Sapalski
     */
    private class PlayerEventRunner extends BukkitRunnable {

        private final String option;

        public PlayerEventRunner(String option) {
            this.option = option;
        }

        public void run() {
            new ResponsePrinter(option).runTaskAsynchronously(BetonQuest.getInstance());
        }
    }

    /**
     * Prints the NPC response to the player. Should be called asynchronously.
     *
     * @author Jakub Sapalski
     */
    private class ResponsePrinter extends BukkitRunnable {

        private String option;

        public ResponsePrinter(String option) {
            this.option = option;
        }

        public void run() {
            // don't forget to select the option prior to printing its text
            selectOption(data.getPointers(uuid, option, OptionType.PLAYER), false);
            // print to player npc's answer
            printNPCText();
            ConversationOptionEvent event = new ConversationOptionEvent(player, conv, option, conv.option);

            new BukkitRunnable() {

                @Override
                public void run() {
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    // fire events
                    for (EventID event : data.getEventIDs(uuid, option, OptionType.PLAYER)) {
                        QuestManager.event(uuid, event);
                    }
                }
            }.runTask(BetonQuest.getInstance());
        }
    }

    /**
     * Prints the options to the player. Should be called asynchronously.
     *
     * @author Jakub Sapalski
     */
    private class OptionPrinter extends BukkitRunnable {

        private String option;

        public OptionPrinter(String option) {
            this.option = option;
        }

        public void run() {
            // fire events
            for (EventID event : data.getEventIDs(uuid, option, OptionType.NPC)) {
                QuestManager.event(uuid, event);
            }

            // print options
            printOptions(data.getPointers(uuid, option, OptionType.NPC));
        }
    }

    /**
     * Ends the conversation. Should be called in the main thread.
     *
     * @author Jakub Sapalski
     */
    private class ConversationEnder extends BukkitRunnable {
        public void run() {
            endConversation();
        }
    }
}
