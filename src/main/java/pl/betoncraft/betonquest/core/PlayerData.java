package pl.betoncraft.betonquest.core;

import lombok.Getter;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.config.QuestCanceler;
import pl.betoncraft.betonquest.core.id.ObjectiveID;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.Scheduler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@Getter
public class PlayerData {

    private final UUID uuid;
    private final List<String> tags;
    private final List<String> journals;
    private final List<Point> points;
    private final HashMap<String, String> objectives;
    private Journal journal;

    public PlayerData(UUID uuid, List<String> tags, LinkedList<String> journals, List<Point> points, HashMap<String, String> objectives) {
        this.uuid = uuid;
        this.tags = tags;
        this.journals = journals;
        this.points = points;
        this.objectives = objectives;
        this.journal = new Journal(uuid, journals);
    }

    public void cancelQuest(String name) {
        QuestCanceler canceler = BetonQuest.getQuestManager().getCancelers().get(name);
        if (canceler != null)
            canceler.cancel(uuid);
    }

    public void startObjectives() {
        for (String objective : objectives.keySet()) {
            try {
                ObjectiveID objectiveID = new ObjectiveID(objective);
                QuestManager.resumeObjective(uuid, objectiveID, objectives.get(objective));
            } catch (ObjectNotFoundException e) {
                LogUtils.getLogger().log(Level.WARNING, "Loaded '" + objective + "' objective from the database, but it is not defined in configuration. Skipping.");
                LogUtils.logThrowable(e);
            }
        }
        objectives.clear();
    }

    public HashMap<String, String> getRawObjectives() {
        return objectives;
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            Scheduler.runAsync(() -> BetonQuest.getStorageManager().updateTag(uuid, tag));
        }
    }

    public void addNewRawObjective(ObjectiveID objectiveID) {
        Objective obj = BetonQuest.getQuestManager().getObjective(objectiveID);
        if (obj == null) {
            return;
        }
        String data = obj.getDefaultDataInstruction();
        if (addRawObjective(objectiveID.toString(), data)) {
            Scheduler.runAsync(() -> BetonQuest.getStorageManager().updateObjective(uuid, objectiveID.toString(), data));
        }
    }

    public boolean addRawObjective(String objectiveID, String data) {
        if (objectives.containsKey(objectiveID)) {
            return false;
        }
        objectives.put(objectiveID, data);
        return true;
    }

    public void addObjToDB(String objectiveID, String data) {
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().updateObjective(uuid, objectiveID, data));
    }

    public void modifyPoints(String category, int count) {
        for (Point point : points) {
            if (point.getCategory().equalsIgnoreCase(category)) {
                Scheduler.runAsync(() -> BetonQuest.getStorageManager().updatePoints(uuid, category, point.getCount() + count));
                point.addPoints(count);
                return;
            }
        }

        points.add(new Point(category, count));
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().insertPoints(uuid, category, count));
    }

    public void removeTag(String tag) {
        tags.remove(tag);
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().deleteTag(uuid, tag));
    }

    public void removePointsCategory(String category) {
        for (Point point : points) {
            if (point.getCategory().equalsIgnoreCase(category)) {
                points.remove(point);
                Scheduler.runAsync(() -> BetonQuest.getStorageManager().deletePoints(uuid, category));
                return;
            }
        }
    }

    public void removeRawObjective(ObjectiveID objectiveID) {
        objectives.remove(objectiveID.toString());
        removeObjFromDB(objectiveID.toString());
    }

    public void removeObjFromDB(String objectiveID) {
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().deleteObjective(uuid, objectiveID));
    }

    public void purgePlayer() {
        for (Objective obj : BetonQuest.getQuestManager().getPlayerObjectives(uuid)) {
            obj.removePlayer(uuid);
        }
        // clear all lists
        objectives.clear();
        tags.clear();
        points.clear();
        journals.clear();
        getJournal().clear();
        getJournal().update();
        // clear the database
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().clearPlayerDate(uuid));
    }
}
