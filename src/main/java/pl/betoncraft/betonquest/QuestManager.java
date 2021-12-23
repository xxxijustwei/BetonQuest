package pl.betoncraft.betonquest;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.api.Variable;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.compatibility.Compatibility;
import pl.betoncraft.betonquest.conditions.*;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.config.QuestCanceler;
import pl.betoncraft.betonquest.conversation.ConversationData;
import pl.betoncraft.betonquest.conversation.ConversationIO;
import pl.betoncraft.betonquest.conversation.Interceptor;
import pl.betoncraft.betonquest.conversation.SimpleInterceptor;
import pl.betoncraft.betonquest.conversation.sub.*;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.VariableInstruction;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.core.id.EventID;
import pl.betoncraft.betonquest.core.id.ObjectiveID;
import pl.betoncraft.betonquest.core.id.VariableID;
import pl.betoncraft.betonquest.events.*;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.notify.*;
import pl.betoncraft.betonquest.objectives.*;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;
import pl.betoncraft.betonquest.variables.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class QuestManager {

    private final BetonQuest plugin;
    private final HashMap<String, Class<? extends Condition>> conditionTypes;
    private final HashMap<String, Class<? extends QuestEvent>> eventTypes;
    private final HashMap<String, Class<? extends Objective>> objectiveTypes;
    private final HashMap<String, Class<? extends ConversationIO>> convIOTypes;
    private final HashMap<String, Class<? extends Interceptor>> interceptorTypes;
    private final HashMap<String, Class<? extends NotifyIO>> notifyIOTypes;
    private final HashMap<String, Class<? extends Variable>> variableTypes;
    private final HashMap<ConditionID, Condition> conditions;
    private final HashMap<EventID, QuestEvent> events;
    private final HashMap<ObjectiveID, Objective> objectives;
    private final HashMap<String, ConversationData> conversations;
    private final HashMap<VariableID, Variable> variables;

    private final HashMap<String, QuestCanceler> cancelers;

    public QuestManager(BetonQuest plugin) {
        this.plugin = plugin;
        this.conditionTypes = new HashMap<>();
        this.eventTypes = new HashMap<>();
        this.objectiveTypes = new HashMap<>();
        this.convIOTypes = new HashMap<>();
        this.interceptorTypes = new HashMap<>();
        this.notifyIOTypes = new HashMap<>();
        this.variableTypes = new HashMap<>();
        this.conditions = new HashMap<>();
        this.events = new HashMap<>();
        this.objectives = new HashMap<>();
        this.conversations = new HashMap<>();
        this.variables = new HashMap<>();
        this.cancelers = new HashMap<>();
    }

    public void init() {
        this.registerInternal();
        new Compatibility();
        this.loadSetting();
        this.loadCanceler();
    }

    public ArrayList<Objective> getPlayerObjectives(UUID uuid) {
        ArrayList<Objective> list = new ArrayList<>();
        for (Objective objective : objectives.values()) {
            if (objective.containsPlayer(uuid)) {
                list.add(objective);
            }
        }
        return list;
    }

    public Objective getObjective(ObjectiveID objectiveID) {
        for (Map.Entry<ObjectiveID, Objective> e : objectives.entrySet()) {
            if (e.getKey().equals(objectiveID)) {
                return e.getValue();
            }
        }
        return null;
    }

    public String getVariableValue(String name, UUID uuid) {
        try {
            Variable var = createVariable(name);
            if (var == null)
                return name;
            return var.getValue(uuid);
        } catch (InstructionParseException e) {
            LogUtils.getLogger().log(Level.WARNING, "Could not create variable: " + e.getMessage());
            LogUtils.logThrowable(e);
            return "could not resolve variable";
        }
    }

    public ConversationData getConversation(String name) {
        return conversations.get(name);
    }

    private void loadCanceler() {
        ConfigurationSection section = FileManager.getPackages().getMain().getYaml().getConfigurationSection("cancel");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                cancelers.put(key, new QuestCanceler(key));
            } catch (InstructionParseException | ObjectNotFoundException e) {
                LogUtils.getLogger().log(Level.WARNING, "Could not load '" + key + "' quest canceler: " + e.getMessage());
                LogUtils.logThrowable(e);
            }
        }
    }

    private void loadSetting() {
        events.clear();
        objectives.clear();
        conversations.clear();
        variables.clear();

        objectives.values().forEach(Objective::close);

        ConfigPackage pack = FileManager.getPackages();

        for (String key : pack.getEvents().keySet()) {

            if (key.contains(" ")) {
                LogUtils.getLogger().log(Level.WARNING, "Event name cannot contain spaces: '" + key + "'");
                continue;
            }

            try {
                EventID id = new EventID(key);
                String type = id.generateInstruction().getPart(0);
                Class<? extends QuestEvent> eventClass = eventTypes.get(type);

                if (eventClass == null) {
                    LogUtils.getLogger().log(Level.WARNING, "Event type " + type + " is not registered, check if it's"
                            + " spelled correctly in '" + id + "' event.");
                    continue;
                }

                QuestEvent event = eventClass.getConstructor(Instruction.class).newInstance(id.generateInstruction());
                events.put(id, event);
                LogUtils.getLogger().log(Level.FINE, "  Event '" + id + "' loaded");
            }
            catch (ObjectNotFoundException e) {
                LogUtils.getLogger().log(Level.WARNING, "Error while loading event '" + key + "': " + e.getMessage());
                LogUtils.logThrowable(e);
            }
            catch (InstructionParseException e) {
                LogUtils.getLogger().log(Level.WARNING, "Event type not defined in '" + key + "'");
                LogUtils.logThrowable(e);
            }
            catch (Exception e) {
                LogUtils.logThrowableReport(e);
            }
        }

        for (String key : FileManager.getPackages().getConditions().keySet()) {

            if (key.contains(" ")) {
                LogUtils.getLogger().log(Level.WARNING, "Condition name cannot contain spaces: '" + key + "'");
                continue;
            }

            try {
                ConditionID id = new ConditionID(key);
                String type = id.generateInstruction().getPart(0);
                Class<? extends Condition> conditionClass = conditionTypes.get(type);

                if (conditionClass == null) {
                    LogUtils.getLogger().log(Level.WARNING, "Condition type " + type + " is not registered,"
                            + " check if it's spelled correctly in '" + id + "' condition.");
                    continue;
                }

                Condition condition = conditionClass.getConstructor(Instruction.class).newInstance(id.generateInstruction());
                conditions.put(id, condition);
                LogUtils.getLogger().log(Level.FINE, "  Condition '" + id + "' loaded");
            }
            catch (ObjectNotFoundException e) {
                LogUtils.getLogger().log(Level.WARNING, "Error while loading condition '" + key + "': " + e.getMessage());
                LogUtils.logThrowable(e);
            }
            catch (InstructionParseException e) {
                LogUtils.getLogger().log(Level.WARNING, "Condition type not defined in '" + key + "'");
                LogUtils.logThrowable(e);
            }
            catch (Exception e) {
                LogUtils.logThrowableReport(e);
            }
        }

        for (String key : FileManager.getPackages().getObjectives().keySet()) {

            if (key.contains(" ")) {
                LogUtils.getLogger().log(Level.WARNING, "Condition name cannot contain spaces: '" + key + "'");
                continue;
            }

            try {
                ObjectiveID id = new ObjectiveID(key);
                String type = id.generateInstruction().getPart(0);
                Class<? extends Objective> objectiveClass = objectiveTypes.get(type);

                if (objectiveClass == null) {
                    LogUtils.getLogger().log(Level.WARNING, "Objective type " + type + " is not registered, check if it's"
                            + " spelled correctly in '" + id + "' objective.");
                    continue;
                }

                Objective objective = objectiveClass.getConstructor(Instruction.class).newInstance(id.generateInstruction());
                objectives.put(id, objective);
                LogUtils.getLogger().log(Level.FINE, "  Objective '" + id + "' loaded");
            }
            catch (ObjectNotFoundException e) {
                LogUtils.getLogger().log(Level.WARNING, "Error while loading objective '" + key + "': " + e.getMessage());
                LogUtils.logThrowable(e);
            }
            catch (InstructionParseException e) {
                LogUtils.getLogger().log(Level.WARNING, "Objective type not defined in '" + key + "'");
                LogUtils.logThrowable(e);
            }
            catch (Exception e) {
                LogUtils.logThrowableReport(e);
            }
        }

        for (String key : FileManager.getPackages().getConversationNames()) {
            if (key.contains(" ")) {
                LogUtils.getLogger().log(Level.WARNING, "Conversation name cannot contain spaces: '" + key + "'");
                continue;
            }

            try {
                conversations.put(key, new ConversationData(key));
            } catch (InstructionParseException e) {
                LogUtils.getLogger().log(Level.WARNING, "Error in '" + key + "' conversation: " + e.getMessage());
                LogUtils.logThrowable(e);
            } catch (Exception e) {
                LogUtils.logThrowableReport(e);
            }
        }

        ConversationData.postEnableCheck();
        LogUtils.getLogger().log(Level.FINE, "Everything loaded!");

        Bukkit.getConsoleSender().sendMessage("§6[BetonQuest] Loading configuration: ");
        Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §a- " + conditions.size() + " coditions");
        Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §a- " + events.size() + " events");
        Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §a- " + objectives.size() + " objectives");
        Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §a- " + conversations.size() + " conversations");
        Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §a- " + variables.size() + " variables");
    }

    private void registerInternal() {
        conditionTypes.clear();
        eventTypes.clear();
        objectiveTypes.clear();
        convIOTypes.clear();
        interceptorTypes.clear();
        notifyIOTypes.clear();
        variableTypes.clear();

        registerConditions();
        registerEvents();
        registerObjectives();
        registerConversationIO();
        registerInterceptor();
        registerNotifyIO();
        registerVariable();
    }

    private void registerConditions() {
        registerConditions("health", HealthCondition.class);
        registerConditions("permission", PermissionCondition.class);
        registerConditions("experience", ExperienceCondition.class);
        registerConditions("tag", TagCondition.class);
        registerConditions("globaltag", GlobalTagCondition.class);
        registerConditions("point", PointCondition.class);
        registerConditions("globalpoint", GlobalPointCondition.class);
        registerConditions("and", ConjunctionCondition.class);
        registerConditions("or", AlternativeCondition.class);
        registerConditions("time", TimeCondition.class);
        registerConditions("weather", WeatherCondition.class);
        registerConditions("height", HeightCondition.class);
        registerConditions("item", ItemCondition.class);
        registerConditions("hand", HandCondition.class);
        registerConditions("location", LocationCondition.class);
        registerConditions("armor", ArmorCondition.class);
        registerConditions("effect", EffectCondition.class);
        registerConditions("rating", ArmorRatingCondition.class);
        registerConditions("sneak", SneakCondition.class);
        registerConditions("random", RandomCondition.class);
        registerConditions("journal", JournalCondition.class);
        registerConditions("testforblock", TestForBlockCondition.class);
        registerConditions("empty", EmptySlotsCondition.class);
        registerConditions("party", PartyCondition.class);
        registerConditions("monsters", MonstersCondition.class);
        registerConditions("objective", ObjectiveCondition.class);
        registerConditions("check", CheckCondition.class);
        registerConditions("chestitem", ChestItemCondition.class);
        registerConditions("score", ScoreboardCondition.class);
        registerConditions("riding", VehicleCondition.class);
        registerConditions("world", WorldCondition.class);
        registerConditions("gamemode", GameModeCondition.class);
        registerConditions("achievement", AchievementCondition.class);
        registerConditions("variable", VariableCondition.class);
        registerConditions("fly", FlyingCondition.class);
        registerConditions("biome", BiomeCondition.class);
        registerConditions("dayofweek", DayOfWeekCondition.class);
        registerConditions("partialdate", PartialDateCondition.class);
        registerConditions("realtime", RealTimeCondition.class);
        registerConditions("looking", LookingAtCondition.class);
        registerConditions("facing", FacingCondition.class);
        registerConditions("conversation", ConversationCondition.class);
        registerConditions("mooncycle", MooncycleCondition.class);
    }

    private void registerEvents() {
        registerEvents("objective", ObjectiveEvent.class);
        registerEvents("command", CommandEvent.class);
        registerEvents("tag", TagEvent.class);
        registerEvents("globaltag", GlobalTagEvent.class);
        registerEvents("journal", JournalEvent.class);
        registerEvents("teleport", TeleportEvent.class);
        registerEvents("explosion", ExplosionEvent.class);
        registerEvents("lightning", LightningEvent.class);
        registerEvents("point", PointEvent.class);
        registerEvents("delpoint", DeletePointEvent.class);
        registerEvents("globalpoint", GlobalPointEvent.class);
        registerEvents("give", GiveEvent.class);
        registerEvents("take", TakeEvent.class);
        registerEvents("conversation", ConversationEvent.class);
        registerEvents("kill", KillEvent.class);
        registerEvents("effect", EffectEvent.class);
        registerEvents("deleffect", DelEffectEvent.class);
        registerEvents("spawn", SpawnMobEvent.class);
        registerEvents("killmob", KillMobEvent.class);
        registerEvents("time", TimeEvent.class);
        registerEvents("weather", WeatherEvent.class);
        registerEvents("folder", FolderEvent.class);
        registerEvents("setblock", SetBlockEvent.class);
        registerEvents("damage", DamageEvent.class);
        registerEvents("party", PartyEvent.class);
        registerEvents("clear", ClearEvent.class);
        registerEvents("run", RunEvent.class);
        registerEvents("sudo", SudoEvent.class);
        registerEvents("opsudo", OpSudoEvent.class);
        registerEvents("chestgive", ChestGiveEvent.class);
        registerEvents("chesttake", ChestTakeEvent.class);
        registerEvents("chestclear", ChestClearEvent.class);
        registerEvents("compass", CompassEvent.class);
        registerEvents("cancel", CancelEvent.class);
        registerEvents("score", ScoreboardEvent.class);
        registerEvents("lever", LeverEvent.class);
        registerEvents("door", DoorEvent.class);
        registerEvents("if", IfElseEvent.class);
        registerEvents("variable", VariableEvent.class);
        registerEvents("playsound", PlaysoundEvent.class);
        registerEvents("pickrandom", PickRandomEvent.class);
        registerEvents("xp", EXPEvent.class);
        registerEvents("notify", NotifyEvent.class);
        registerEvents("openshop", OpenShopEvent.class);
        registerEvents("selfmodel", SelfModelEvent.class);
        registerEvents("clothestry", ClothesTryEvent.class);
        registerEvents("clothesbuy", ClothesBuyEvent.class);
    }

    private void registerObjectives() {
        registerObjectives("location", LocationObjective.class);
        registerObjectives("block", BlockObjective.class);
        registerObjectives("mobkill", MobKillObjective.class);
        registerObjectives("action", ActionObjective.class);
        registerObjectives("die", DieObjective.class);
        registerObjectives("craft", CraftingObjective.class);
        registerObjectives("smelt", SmeltingObjective.class);
        registerObjectives("tame", TameObjective.class);
        registerObjectives("delay", DelayObjective.class);
        registerObjectives("arrow", ArrowShootObjective.class);
        registerObjectives("experience", ExperienceObjective.class);
        registerObjectives("step", StepObjective.class);
        registerObjectives("logout", LogoutObjective.class);
        registerObjectives("password", PasswordObjective.class);
        registerObjectives("fish", FishObjective.class);
        registerObjectives("enchant", EnchantObjective.class);
        registerObjectives("shear", ShearObjective.class);
        registerObjectives("chestput", ChestPutObjective.class);
        registerObjectives("potion", PotionObjective.class);
        registerObjectives("vehicle", VehicleObjective.class);
        registerObjectives("consume", ConsumeObjective.class);
        registerObjectives("variable", VariableObjective.class);
        registerObjectives("kill", KillPlayerObjective.class);
        registerObjectives("breed", BreedObjective.class);
        registerObjectives("interact", EntityInteractObjective.class);
        registerObjectives("respawn", RespawnObjective.class);
    }

    private void registerConversationIO() {
        registerConversationIO("simple", SimpleConvIO.class);
        registerConversationIO("tellraw", TellrawConvIO.class);
        registerConversationIO("chest", InventoryConvIO.class);
        registerConversationIO("combined", InventoryConvIO.Combined.class);
        registerConversationIO("slowtellraw", SlowTellrawConvIO.class);
        registerConversationIO("screen", ScreenConvIO.class);
    }

    private void registerInterceptor() {
        registerInterceptor("simple", SimpleInterceptor.class);
    }

    private void registerNotifyIO() {
        registerNotifyIO("suppress", SuppressNotifyIO.class);
        registerNotifyIO("chat", ChatNotifyIO.class);
        registerNotifyIO("advancement", AdvancementNotifyIO.class);
        registerNotifyIO("actionbar", ActionBarNotifyIO.class);
        registerNotifyIO("bossbar", BossBarNotifyIO.class);
        registerNotifyIO("title", TitleNotifyIO.class);
        registerNotifyIO("subtitle", SubTitleNotifyIO.class);
    }

    private void registerVariable() {
        registerVariable("player", PlayerNameVariable.class);
        registerVariable("npc", NpcNameVariable.class);
        registerVariable("objective", ObjectivePropertyVariable.class);
        registerVariable("point", PointVariable.class);
        registerVariable("globalpoint", GlobalPointVariable.class);
        registerVariable("item", ItemAmountVariable.class);
        registerVariable("version", VersionVariable.class);
        registerVariable("location", LocationVariable.class);
        registerVariable("math", MathVariable.class);
    }

    public void registerConditions(String name, Class<? extends Condition> conditionClass) {
        LogUtils.getLogger().log(Level.FINE, "Registering " + name + " condition type");
        conditionTypes.put(name, conditionClass);
    }

    public void registerEvents(String name, Class<? extends QuestEvent> eventClass) {
        LogUtils.getLogger().log(Level.FINE, "Registering " + name + " event type");
        eventTypes.put(name, eventClass);
    }

    public void registerObjectives(String name, Class<? extends Objective> objectiveClass) {
        LogUtils.getLogger().log(Level.FINE, "Registering " + name + " objective type");
        objectiveTypes.put(name, objectiveClass);
    }

    public void registerConversationIO(String name, Class<? extends ConversationIO> convIOClass) {
        LogUtils.getLogger().log(Level.FINE, "Registering " + name + " conversation IO type");
        convIOTypes.put(name, convIOClass);
    }

    public void registerInterceptor(String name, Class<? extends Interceptor> interceptorClass) {
        LogUtils.getLogger().log(Level.FINE, "Registering " + name + " interceptor type");
        interceptorTypes.put(name, interceptorClass);
    }

    public void registerNotifyIO(String name, Class<? extends NotifyIO> IOClass) {
        LogUtils.getLogger().log(Level.FINE, "Registering " + name + " notify IO type");
        notifyIOTypes.put(name, IOClass);
    }

    public void registerVariable(String name, Class<? extends Variable> variable) {
        LogUtils.getLogger().log(Level.FINE, "Registering " + name + " variable type");
        variableTypes.put(name, variable);
    }

    public static Class<? extends ConversationIO> getConvIO(String name) {
        return BetonQuest.getQuestManager().getConvIOTypes().get(name);
    }

    public static Class<? extends Interceptor> getInterceptor(String name) {
        return BetonQuest.getQuestManager().getInterceptorTypes().get(name);
    }

    public static Class<? extends QuestEvent> getEventClass(String name) {
        return BetonQuest.getQuestManager().getEventTypes().get(name);
    }

    public static Class<? extends Condition> getConditionClass(String name) {
        return BetonQuest.getQuestManager().getConditionTypes().get(name);
    }

    public static Class<? extends NotifyIO> getNotifyIO(String name) {
        return BetonQuest.getQuestManager().getNotifyIOTypes().get(name);
    }

    public static Variable createVariable(String instruction) throws InstructionParseException {
        VariableID ID;
        try {
            ID = new VariableID(instruction);
        } catch (ObjectNotFoundException e) {
            throw new InstructionParseException("Could not load variable: " + e.getMessage(), e);
        }
        // no need to create duplicated variables
        for (Map.Entry<VariableID, Variable> e : BetonQuest.getQuestManager().getVariables().entrySet()) {
            if (e.getKey().equals(ID)) {
                return e.getValue();
            }
        }
        String[] parts = instruction.replace("%", "").split("\\.");
        if (parts.length < 1) {
            throw new InstructionParseException("Not enough arguments in variable " + ID);
        }
        Class<? extends Variable> variableClass = BetonQuest.getQuestManager().getVariableTypes().get(parts[0]);
        // if it's null then there is no such type registered, log an error
        if (variableClass == null) {
            throw new InstructionParseException("Variable type " + parts[0] + " is not registered");
        }
        try {
            Variable variable = variableClass.getConstructor(Instruction.class).newInstance(new VariableInstruction(null, instruction));
            BetonQuest.getQuestManager().getVariables().put(ID, variable);
            LogUtils.getLogger().log(Level.FINE, "Variable " + ID + " loaded");
            return variable;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof InstructionParseException) {
                throw new InstructionParseException("Error in " + ID + " variable: " + e.getCause().getMessage(), e);
            } else {
                LogUtils.logThrowableReport(e);
            }
        } catch (Exception e) {
            LogUtils.logThrowableReport(e);
        }
        return null;
    }

    public static ArrayList<String> resolveVariables(String text) {
        ArrayList<String> variables = new ArrayList<>();
        Matcher matcher = Pattern.compile("%[^ %\\s]+%").matcher(text);
        while (matcher.find()) {
            String variable = matcher.group();
            if (!variables.contains(variable)) variables.add(variable);
        }
        return variables;
    }

    public static boolean condition(UUID uuid, ConditionID conditionID) {
        // null check
        if (conditionID == null) {
            LogUtils.getLogger().log(Level.FINE, "Null condition ID!");
            return false;
        }
        // get the condition
        Condition condition = null;
        for (Map.Entry<ConditionID, Condition> e : BetonQuest.getQuestManager().getConditions().entrySet()) {
            if (e.getKey().equals(conditionID)) {
                condition = e.getValue();
                break;
            }
        }
        if (condition == null) {
            LogUtils.getLogger().log(Level.WARNING, "The condition " + conditionID + " is not defined!");
            return false;
        }
        // check for null player
        if (uuid == null && !condition.isStatic()) {
            LogUtils.getLogger().log(Level.FINE, "Cannot check non-static condition without a player, returning false");
            return false;
        }
        // check for online player
        if (uuid != null && PlayerConverter.getPlayer(uuid) == null && !condition.isPersistent()) {
            LogUtils.getLogger().log(Level.FINE, "Player was offline, condition is not persistent, returning false");
            return false;
        }
        // and check if it's met or not
        boolean outcome;
        try {
            outcome = condition.check(uuid);
        } catch (QuestRuntimeException e) {
            LogUtils.getLogger().log(Level.WARNING, "Error while checking '" + conditionID + "' condition: " + e.getMessage());
            LogUtils.logThrowable(e);
            return false;
        }
        boolean isMet = (outcome && !conditionID.inverted()) || (!outcome && conditionID.inverted());
        LogUtils.getLogger().log(Level.FINE, (isMet ? "TRUE" : "FALSE") + ": " + (conditionID.inverted() ? "inverted" : "") + " condition "
                + conditionID + " for player " + PlayerConverter.getName(uuid));
        return isMet;
    }

    public static void event(UUID uuid, EventID eventID) {
        // null check
        if (eventID == null) {
            LogUtils.getLogger().log(Level.FINE, "Null event ID!");
            return;
        }
        // get the event
        QuestEvent event = null;
        for (Map.Entry<EventID, QuestEvent> e : BetonQuest.getQuestManager().getEvents().entrySet()) {
            if (e.getKey().equals(eventID)) {
                event = e.getValue();
                break;
            }
        }
        if (event == null) {
            LogUtils.getLogger().log(Level.WARNING, "Event " + eventID + " is not defined");
            return;
        }
        // fire the event
        if (uuid == null) {
            LogUtils.getLogger().log(Level.FINE, "Firing static event " + eventID);
        } else {
            LogUtils.getLogger().log(Level.FINE, "Firing event " + eventID + " for " + PlayerConverter.getName(uuid));
        }
        try {
            event.fire(uuid);
        } catch (QuestRuntimeException e) {
            LogUtils.getLogger().log(Level.WARNING, "Error while firing '" + eventID + "' event: " + e.getMessage());
            LogUtils.logThrowable(e);
        }
    }

    public static void newObjective(UUID uuid, ObjectiveID objectiveID) {
        // null check
        if (uuid == null || objectiveID == null) {
            LogUtils.getLogger().log(Level.FINE, "Null arguments for the objective!");
            return;
        }
        Objective objective = null;
        for (Map.Entry<ObjectiveID, Objective> e : BetonQuest.getQuestManager().getObjectives().entrySet()) {
            if (e.getKey().equals(objectiveID)) {
                objective = e.getValue();
                break;
            }
        }
        if (objective == null) {
            LogUtils.getLogger().log(Level.WARNING, "Objective " + objectiveID + " is not defined");
            return;
        }

        if (objective.containsPlayer(uuid)) {
            LogUtils.getLogger().log(Level.FINE, "Player " + PlayerConverter.getName(uuid) + " already has the " + objectiveID +
                    " objective");
            return;
        }
        objective.newPlayer(uuid);
    }

    public static void resumeObjective(UUID uuid, ObjectiveID objectiveID, String instruction) {
        // null check
        if (uuid == null || objectiveID == null || instruction == null) {
            LogUtils.getLogger().log(Level.FINE, "Null arguments for the objective!");
            return;
        }
        Objective objective = null;
        for (Map.Entry<ObjectiveID, Objective> e : BetonQuest.getQuestManager().getObjectives().entrySet()) {
            if (e.getKey().equals(objectiveID)) {
                objective = e.getValue();
                break;
            }
        }
        if (objective == null) {
            LogUtils.getLogger().log(Level.WARNING, "Objective " + objectiveID + " does not exist");
            return;
        }
        if (objective.containsPlayer(uuid)) {
            LogUtils.getLogger().log(Level.FINE,
                    "Player " + PlayerConverter.getName(uuid) + " already has the " + objectiveID + " objective!");
            return;
        }
        objective.addPlayer(uuid, instruction);
    }
}
