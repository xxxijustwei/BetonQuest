package pl.betoncraft.betonquest.storage;

import net.sakuragame.serversystems.manage.api.database.DataManager;
import net.sakuragame.serversystems.manage.api.database.DatabaseQuery;
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.core.Point;

import java.sql.*;
import java.util.*;

public class StorageManager {

    private final BetonQuest plugin;
    private final DataManager dataManager;

    public StorageManager(BetonQuest plugin) {
        this.plugin = plugin;
        this.dataManager = ClientManagerAPI.getDataManager();
    }

    public void init() {
        for (QuestTables table : QuestTables.values()) {
            table.createTable();
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        int uid = ClientManagerAPI.getUserID(uuid);

        List<String> tags = new ArrayList<>();
        List<String> journals = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        HashMap<String, String> objectives = new HashMap<>();

        try (DatabaseQuery query = dataManager.createQuery(QuestTables.QUEST_OBJECTIVES.getTableName(), "uid", uid)) {
            ResultSet result = query.getResultSet();
            while (result.next()) {
                String objective = result.getString("objective");
                String instructions = result.getString("instructions");
                objectives.put(objective, instructions);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        try (DatabaseQuery query = dataManager.createQuery(QuestTables.QUEST_TAGS.getTableName(), "uid", uid)) {
            ResultSet result = query.getResultSet();
            while (result.next()) {
                String tag = result.getString("tag");
                tags.add(tag);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        try (DatabaseQuery query = dataManager.createQuery(QuestTables.QUEST_JOURNAL.getTableName(), "uid", uid)) {
            ResultSet result = query.getResultSet();
            while (result.next()) {
                String pointer = result.getString("pointer");
                journals.add(pointer);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        try (DatabaseQuery query = dataManager.createQuery(QuestTables.QUEST_POINTS.getTableName(), "uid", uid)) {
            ResultSet result = query.getResultSet();
            while (result.next()) {
                String category = result.getString("category");
                int count = result.getInt("count");
                points.add(new Point(category, count));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return new PlayerData(uuid, tags, journals, points, objectives);
    }

    public void clearPlayerDate(UUID uuid) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeDelete(QuestTables.QUEST_OBJECTIVES.getTableName(), "uid", uid);
        dataManager.executeDelete(QuestTables.QUEST_JOURNAL.getTableName(), "uid", uid);
        dataManager.executeDelete(QuestTables.QUEST_POINTS.getTableName(), "uid", uid);
        dataManager.executeDelete(QuestTables.QUEST_TAGS.getTableName(), "uid", uid);
    }

    public void insertJournal(UUID uuid, String pointer) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeInsert(
                QuestTables.QUEST_JOURNAL.getTableName(),
                new String[] {"uid", "pointer"},
                new Object[] {uid, pointer}
        );
    }

    public void insertPoints(UUID uuid, String category, int count) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeInsert(
                QuestTables.QUEST_POINTS.getTableName(),
                new String[] {"uid", "category", "count"},
                new Object[] {uid, category, count}
        );
    }

    public void updatePoints(UUID uuid, String category, int count) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeReplace(
                QuestTables.QUEST_POINTS.getTableName(),
                new String[] {"uid", "category", "count"},
                new Object[] {uid, category, count}
        );
    }

    public void updateObjective(UUID uuid, String objective, String Instruction) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeReplace(
                QuestTables.QUEST_OBJECTIVES.getTableName(),
                new String[] {"uid", "objective", "instructions"},
                new Object[] {uid, objective, Instruction}
        );
    }

    public void updateTag(UUID uuid, String tag) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeReplace(
                QuestTables.QUEST_TAGS.getTableName(),
                new String[] {"uid", "tag"},
                new Object[] {uid, tag}
        );
    }

    public void deletePoints(UUID uuid, String category) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeDelete(
                QuestTables.QUEST_POINTS.getTableName(),
                new String[] {"uid", "category"},
                new Object[] {uid, category}
        );
    }

    public void deleteObjective(UUID uuid, String objective) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeDelete(
                QuestTables.QUEST_OBJECTIVES.getTableName(),
                new String[] {"uid", "objective"},
                new Object[] {uid, objective}
        );
    }

    public void deleteTag(UUID uuid, String tag) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeDelete(
                QuestTables.QUEST_TAGS.getTableName(),
                new String[] {"uid", "tag"},
                new Object[] {uid, tag}
        );
    }

    public void deleteJournal(UUID uuid, String pointer) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeDelete(
                QuestTables.QUEST_JOURNAL.getTableName(),
                new String[] {"uid", "pointer"},
                new Object[] {uid, pointer}
        );
    }

    public List<String> getGlobalTags() {
        List<String> tags = new ArrayList<>();

        try (DatabaseQuery query = dataManager.createQueryInTable(QuestTables.QUEST_GLOBAL_TAGS.getTableName())) {
            ResultSet result = query.getResultSet();
            while (result.next()) {
                tags.add(result.getString("tag"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return tags;
    }

    public List<Point> getGlobalPoints() {
        List<Point> points = new ArrayList<>();

        try (DatabaseQuery query = dataManager.createQueryInTable(QuestTables.QUEST_GLOBAL_POINTS.getTableName())) {
            ResultSet result = query.getResultSet();
            while (result.next()) {
                String category = result.getString("category");
                int count = result.getInt("count");

                points.add(new Point(category, count));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return points;
    }

    public void updateGlobalPoints(String category, int count) {
        dataManager.executeReplace(
                QuestTables.QUEST_GLOBAL_POINTS.getTableName(),
                new String[] {"category", "count"},
                new Object[] {category, count}
        );
    }

    public void insertGlobalPoints(String category, int count) {
        dataManager.executeInsert(
                QuestTables.QUEST_GLOBAL_POINTS.getTableName(),
                new String[] {"category", "count"},
                new Object[] {category, count}
        );
    }

    public void insertGlobalTag(String tag) {
        dataManager.executeInsert(
                QuestTables.QUEST_GLOBAL_TAGS.getTableName(),
                new String[] {"tag"},
                new Object[] {tag}
        );
    }

    public void deleteGlobalTag(String tag) {
        dataManager.executeDelete(
                QuestTables.QUEST_GLOBAL_TAGS.getTableName(),
                "tag", tag
        );
    }

    public void deleteGlobalPoints(String category) {
        dataManager.executeDelete(
                QuestTables.QUEST_GLOBAL_POINTS.getTableName(),
                "category", category
        );
    }

    public void deleteGlobalTags() {
        dataManager.executeSQL("DELETE FROM " + QuestTables.QUEST_GLOBAL_TAGS.getTableName());
    }

    public void deleteGlobalPoints() {
        dataManager.executeSQL("DELETE FROM " + QuestTables.QUEST_GLOBAL_POINTS.getTableName());
    }
}
