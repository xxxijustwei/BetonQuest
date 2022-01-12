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

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.core.id.EventID;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.Utils;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Represents the data of the conversation.
 *
 * @author Jakub Sapalski
 */
public class ConversationData {

    private final static ArrayList<String> externalPointers = new ArrayList<>();

    private final String convName;

    @Getter private final int npcID;
    @Getter private final double modelScale;
    private final String quester; // maps for multiple languages
    private final EventID[] finalEvents;
    private final String[] startingOptions;
    private final boolean blockMovement;
    private String convIO;
    private String interceptor;

    private final HashMap<String, Option> NPCOptions;
    private final HashMap<String, Option> playerOptions;

    /**
     * Loads conversation from package.
     *
     * @param name the name of the conversation
     * @throws InstructionParseException when there is a syntax error in the defined conversation
     */
    public ConversationData(String name) throws InstructionParseException {
        this.convName = name;
        ConfigPackage pack = FileManager.getPackages();
        LogUtils.getLogger().log(Level.FINE, String.format("Loading %s conversation", name));

        YamlConfiguration conv = pack.getConversation(name).getYaml();

        npcID = conv.getInt("npc-id");
        modelScale = conv.getDouble("model-scale");
        quester = conv.getString("quester");
        String rawFinalEvents = conv.getString("final_events");
        String rawStartingOptions = conv.getString("first");
        String stop = conv.getString("stop");
        blockMovement = stop != null && stop.equalsIgnoreCase("true");
        String rawConvIO = conv.getString("conversationIO", BetonQuest.getFileManager().getConfig().getString("default_conversation_IO", "menu,chest"));
        String rawInterceptor = conv.getString("interceptor", BetonQuest.getFileManager().getConfig().getString("default_interceptor", "simple"));

        // check if all data is valid (or at least exist)
        for (String s : rawConvIO.split(",")) {
            if (QuestManager.getConvIO(s.trim()) != null) {
                convIO = s.trim();
                break;
            }
        }
        if (convIO == null) {
            throw new InstructionParseException("No registered conversation IO found: " + rawConvIO);
        }

        for (String s : rawInterceptor.split(",")) {
            if (QuestManager.getInterceptor(s.trim()) != null) {
                interceptor = s.trim();
                break;
            }
        }
        if (interceptor == null) {
            throw new InstructionParseException("No registered interceptor found: " + rawInterceptor);
        }

        if (quester == null || quester.isEmpty()) {
            throw new InstructionParseException("Quester's name is not defined");
        }

        if (rawStartingOptions == null || rawStartingOptions.equals("")) {
            throw new InstructionParseException("Starting options are not defined");
        }

        if (rawFinalEvents != null && !rawFinalEvents.equals("")) {
            String[] array = rawFinalEvents.split(",");
            finalEvents = new EventID[array.length];
            for (int i = 0; i < array.length; i++) {
                try {
                    finalEvents[i] = new EventID(array[i]);
                } catch (ObjectNotFoundException e) {
                    throw new InstructionParseException("Error while loading final events: " + e.getMessage(), e);
                }
            }
        } else {
            finalEvents = new EventID[0];
        }
        // load all NPC options
        ConfigurationSection NPCSection = pack.getConversation(name).getYaml().getConfigurationSection("npc_options");
        if (NPCSection == null) {
            throw new InstructionParseException("npc_options section not defined");
        }
        NPCOptions = new HashMap<>();
        for (String key : NPCSection.getKeys(false)) {
            NPCOptions.put(key, new NPCOption(key));
        }
        // check if all starting options point to existing NPC options
        startingOptions = rawStartingOptions.split(",");
        // remove spaces between the options
        for (int i = 0; i < startingOptions.length; i++) {
            startingOptions[i] = startingOptions[i].trim();
        }
        for (String startingOption : startingOptions) {
            if (startingOption.contains(".")) {
                String entirePointer = convName + ".<starting_option>." + startingOption;
                externalPointers.add(entirePointer);
            } else if (!NPCOptions.containsKey(startingOption)) {
                throw new InstructionParseException("Starting option " + startingOption + " does not exist");
            }
        }
        // load all Player options
        ConfigurationSection playerSection = pack.getConversation(name).getYaml()
                .getConfigurationSection("player_options");
        playerOptions = new HashMap<>();
        if (playerSection != null) {
            for (String key : playerSection.getKeys(false)) {
                playerOptions.put(key, new PlayerOption(key));
            }
        }

        // check if every pointer points to existing option.
        for (Option option : NPCOptions.values()) {
            for (String pointer : option.getPointers()) {
                if (!playerOptions.containsKey(pointer)) {
                    throw new InstructionParseException(
                            String.format("NPC option %s points to %s player option, but it does not exist",
                                    option.getName(), pointer));
                }
            }
            for (String extend : option.getExtends()) {
                if (!NPCOptions.containsKey(extend)) {
                    throw new InstructionParseException(
                            String.format("NPC option %s extends %s, but it does not exist",
                                    option.getName(), extend));
                }
            }
        }
        for (Option option : playerOptions.values()) {
            for (String pointer : option.getPointers()) {
                if (pointer.contains(".")) {
                    String entirePointer = convName + "." + option.getName() + "." + pointer;
                    externalPointers.add(entirePointer);
                } else if (!NPCOptions.containsKey(pointer)) {
                    throw new InstructionParseException(
                            String.format("Player option %s points to %s NPC option, but it does not exist",
                                    option.getName(), pointer));
                }
            }
            for (String extend : option.getExtends()) {
                if (!playerOptions.containsKey(extend)) {
                    throw new InstructionParseException(
                            String.format("Player option %s extends %s, but it does not exist",
                                    option.getName(), extend));
                }
            }
        }

        // done, everything will work
        LogUtils.getLogger().log(Level.FINE, String.format("Conversation loaded: %d NPC options and %d player options", NPCOptions.size(),
                playerOptions.size()));
    }

    /**
     * Checks if external pointers point to valid options. It cannot be checked
     * when constructing ConversationData objects because conversations that are
     * being pointed to may not yet exist.
     * <p>
     * This method should be called when all conversations are loaded. It will
     * not throw any exceptions, just display errors in the console.
     */
    public static void postEnableCheck() {
        for (String externalPointer : externalPointers) {
            String[] parts = externalPointer.split("\\.");
            String packName = parts[0];
            String sourceConv = parts[1];
            String sourceOption = parts[2];
            String targetConv = parts[3];
            String targetOption = parts[4];
            ConversationData conv = BetonQuest.getQuestManager().getConversation(targetConv);
            String common = (sourceOption.equals("<starting_option>")) ? "starting option" : ("'" + sourceOption + "' player option");
            if (conv == null) {
                LogUtils.getLogger().log(Level.WARNING, "External pointer in '" + packName + "' package, '" + sourceConv + "' conversation, "
                        + common
                        + " points to '" + targetConv
                        + "' conversation, but it does not even exist. Check your spelling!");
                continue;
            }
            if (conv.getText(targetOption, OptionType.NPC) == null) {
                LogUtils.getLogger().log(Level.WARNING, "External pointer in '" + packName + "' package, '" + sourceConv + "' conversation, "
                        + common
                        + " points to '" + targetOption + "' NPC option in '" + targetConv
                        + "' conversation, but it does not exist.");
            }
        }
        externalPointers.clear();
    }

    /**
     * @return the name of this conversation
     */
    public String getName() {
        return convName;
    }

    /**
     * @return the quester's name
     */
    public String getQuester() {
        return quester;
    }

    /**
     * @return the final events
     */
    public EventID[] getFinalEvents() {
        return finalEvents;
    }

    /**
     * @return the starting options
     */
    public String[] getStartingOptions() {
        return startingOptions;
    }

    /**
     * @return true if movement should be blocked
     */
    public boolean isMovementBlocked() {
        return blockMovement;
    }

    /**
     * @return the conversationIO
     */
    public String getConversationIO() {
        return convIO;
    }

    /**
     * @return the Interceptor
     */
    public String getInterceptor() {
        return interceptor;
    }

    public String getText(String option, OptionType type) {
        return getText(null, option, type);
    }

    public String getText(UUID uuid, String option, OptionType type) {
        Option o;
        if (type == OptionType.NPC) {
            o = NPCOptions.get(option);
        } else {
            o = playerOptions.get(option);
        }
        if (o == null)
            return null;
        return o.getText(uuid);
    }

    public ConditionID[] getConditionIDs(String option, OptionType type) {
        HashMap<String, Option> options;
        if (type == OptionType.NPC) {
            options = NPCOptions;
        } else {
            options = playerOptions;
        }
        return options.get(option).getConditions();
    }

    public EventID[] getEventIDs(UUID uuid, String option, OptionType type) {
        HashMap<String, Option> options = type == OptionType.NPC ? NPCOptions : playerOptions;
        return options.get(option).getEvents(uuid);
    }

    public List<EventID> getExecute(String option) {
        return playerOptions.get(option).getExecute();
    }

    public String[] getPointers(UUID uuid, String option, OptionType type) {
        HashMap<String, Option> options;
        if (type == OptionType.NPC) {
            options = NPCOptions;
        } else {
            options = playerOptions;
        }
        return options.get(option).getPointers(uuid);
    }

    public Option getOption(String option, OptionType type) {
        return type == OptionType.NPC ? NPCOptions.get(option) : playerOptions.get(option);
    }

    /**
     * Check if conversation has at least one valid option for player
     */
    public boolean isReady(UUID uuid) {
        options:
        for (String option : getStartingOptions()) {
            String convName, optionName;
            if (option.contains(".")) {
                String[] parts = option.split("\\.");
                convName = parts[0];
                optionName = parts[1];
            } else {
                convName = getName();
                optionName = option;
            }
            ConversationData currentData = BetonQuest.getQuestManager().getConversation(convName);
            for (ConditionID condition : currentData.getConditionIDs(optionName, ConversationData.OptionType.NPC)) {
                if (!QuestManager.condition(uuid, condition)) {
                    continue options;
                }
            }
            return true;
        }
        return false;
    }

    public enum OptionType {
        NPC, PLAYER
    }

    /**
     * Represents an option
     */
    private abstract class Option {

        private final String name;
        private final OptionType type;

        private String text;
        private final List<ConditionID> conditions = new ArrayList<>();
        private final List<EventID> execute = new ArrayList<>();
        private final List<EventID> events = new ArrayList<>();
        private final List<String> pointers;
        private final List<String> extendLinks;

        public Option(String name, String type, String visibleType) throws InstructionParseException {
            this.name = name;
            this.type = type.equals("player_options") ? OptionType.PLAYER : OptionType.NPC;
            ConfigurationSection conv = FileManager.getPackages().getConversation(convName).getYaml().getConfigurationSection(type + "." + name);

            // Text
            if (conv.contains("text")) {
                text = Utils.format(conv.getString("text"));

                ArrayList<String> variables = new ArrayList<>();

                if (text == null || text.equals("")) throw new InstructionParseException("Text not defined in " + visibleType + " " + name);
                // variables are possibly duplicated because there probably is
                // the same variable in every language
                ArrayList<String> possiblyDuplicatedVariables = QuestManager.resolveVariables(text);
                for (String possiblyDuplicatedVariable : possiblyDuplicatedVariables) {
                    if (variables.contains(possiblyDuplicatedVariable))
                        continue;
                    variables.add(possiblyDuplicatedVariable);
                }

                for (String variable : variables) {
                    try {
                        QuestManager.createVariable(variable);
                    } catch (InstructionParseException e) {
                        throw new InstructionParseException("Error while creating '" + variable + "' variable: "
                                + e.getMessage(), e);
                    }
                }
            }

            // Conditions
            try {
                for (String rawCondition : conv.getString("conditions", conv.getString("condition", "")).split(",")) {
                    if (!Objects.equals(rawCondition, "")) {
                        conditions.add(new ConditionID(rawCondition.trim()));
                    }
                }
            } catch (ObjectNotFoundException e) {
                throw new InstructionParseException("Error in '" + name + "' " + visibleType + " option's conditions: " + e.getMessage(), e);
            }

            // Execute
            try {
                for (String rawEvent : conv.getString("execute", conv.getString("execute", "")).split(",")) {
                    if (!Objects.equals(rawEvent, "")) {
                        execute.add(new EventID(rawEvent.trim()));
                    }
                }
            } catch (ObjectNotFoundException e) {
                throw new InstructionParseException("Error in '" + name + "' " + visibleType + " option's execute: "
                        + e.getMessage(), e);
            }

            // Events
            try {
                for (String rawEvent : conv.getString("events", conv.getString("event", "")).split(",")) {
                    if (!Objects.equals(rawEvent, "")) {
                        events.add(new EventID(rawEvent.trim()));
                    }
                }
            } catch (ObjectNotFoundException e) {
                throw new InstructionParseException("Error in '" + name + "' " + visibleType + " option's events: "
                        + e.getMessage(), e);
            }

            // Pointers
            pointers = Arrays.stream(conv.getString("pointers", conv.getString("pointer", "")).split(","))
                    .filter(StringUtils::isNotEmpty)
                    .map(String::trim)
                    .collect(Collectors.toList());


            extendLinks = Arrays.stream(conv.getString("extends", conv.getString("extend", "")).split(","))
                    .filter(StringUtils::isNotEmpty)
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        public String getName() {
            return name;
        }

        public String getText(UUID uuid) {
            return getText(uuid, new ArrayList<>());
        }

        public String getText(UUID uuid, List<String> optionPath) {
            // Prevent infinite loops
            if (optionPath.contains(getName())) {
                return "";
            }
            optionPath.add(getName());

            StringBuilder ret = new StringBuilder(text);

            if (uuid != null) {
                extend:
                for (String extend : extendLinks) {
                    for (ConditionID condition : getOption(extend, type).getConditions()) {
                        if (!QuestManager.condition(uuid, condition)) {
                            continue extend;
                        }
                    }
                    ret.append(getOption(extend, type).getText(uuid, optionPath));
                    break;
                }
            }

            return ret.toString();
        }

        public ConditionID[] getConditions() {
            return getConditions(new ArrayList<>());
        }

        public ConditionID[] getConditions(List<String> optionPath) {
            // Prevent infinite loops
            if (optionPath.contains(getName())) {
                return new ConditionID[0];
            }
            optionPath.add(getName());

            List<ConditionID> ret = new ArrayList<>(conditions);

            return ret.toArray(new ConditionID[0]);
        }

        public EventID[] getEvents(UUID uuid) {
            return getEvents(uuid, new ArrayList<>());
        }

        public EventID[] getEvents(UUID uuid, List<String> optionPath) {
            // Prevent infinite loops
            if (optionPath.contains(getName())) {
                return new EventID[0];
            }
            optionPath.add(getName());

            List<EventID> ret = new ArrayList<>(events);

            extend:
            for (String extend : extendLinks) {
                for (ConditionID condition : getOption(extend, type).getConditions()) {
                    if (!QuestManager.condition(uuid, condition)) {
                        continue extend;
                    }
                }
                ret.addAll(Arrays.asList(getOption(extend, type).getEvents(uuid, optionPath)));
                break;
            }

            return ret.toArray(new EventID[0]);
        }

        public List<EventID> getExecute() {
            return execute;
        }

        public String[] getPointers() {
            return getPointers(null);
        }

        public String[] getPointers(UUID uuid) {
            return getPointers(uuid, new ArrayList<>());
        }

        public String[] getPointers(UUID uuid, List<String> optionPath) {
            // Prevent infinite loops
            if (optionPath.contains(getName())) {
                return new String[0];
            }
            optionPath.add(getName());

            List<String> ret = new ArrayList<>(pointers);

            if (uuid != null) {
                extend:
                for (String extend : extendLinks) {
                    for (ConditionID condition : getOption(extend, type).getConditions()) {
                        if (!QuestManager.condition(uuid, condition)) {
                            continue extend;
                        }
                    }
                    ret.addAll(Arrays.asList(getOption(extend, type).getPointers(uuid, optionPath)));
                    break;
                }
            }

            return ret.toArray(new String[0]);
        }

        public String[] getExtends() {
            return extendLinks.toArray(new String[0]);
        }
    }

    /**
     * Represents an option which can be choosen by the Player
     */
    private class PlayerOption extends Option {
        public PlayerOption(String name) throws InstructionParseException {
            super(name, "player_options", "player option");
        }
    }

    /**
     * Represents an option which can be choosen by the NPC
     */
    private class NPCOption extends Option {
        public NPCOption(String name) throws InstructionParseException {
            super(name, "npc_options", "NPC option");
        }
    }
}
