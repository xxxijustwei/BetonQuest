package pl.betoncraft.betonquest.storage;

import net.sakuragame.serversystems.manage.api.database.DataManager;
import net.sakuragame.serversystems.manage.api.database.DatabaseQuery;
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.core.Point;
import pl.betoncraft.betonquest.core.Pointer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
        List<Pointer> entries = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        HashMap<String, String> objectives = new HashMap<>();

        try {
            Connection conn = dataManager.getConnection();

            DatabaseQuery objectiveQuery = dataManager.createQuery(QuestTables.QUEST_OBJECTIVES.getTableName(), "uid", uid);
            DatabaseQuery tagQuery = dataManager.createQuery(QuestTables.QUEST_TAGS.getTableName(), "uid", uid);
            DatabaseQuery journalQuery = dataManager.createQuery(QuestTables.QUEST_JOURNAL.getTableName(), "uid", uid);
            DatabaseQuery pointQuery = dataManager.createQuery(QuestTables.QUEST_POINTS.getTableName(), "uid", uid);

            ResultSet oResult = objectiveQuery.getResultSet();
            while (oResult.next()) {
                String objective = oResult.getString("objective");
                String instructions = oResult.getString("instructions");
                objectives.put(objective, instructions);
            }
            oResult.close();

            ResultSet tResult = tagQuery.getResultSet();
            while (tResult.next()) {
                String tag = tResult.getString("tag");
                tags.add(tag);
            }
            tResult.close();

            ResultSet jResult = journalQuery.getResultSet();
            while (jResult.next()) {
                String pointer = jResult.getString("pointer");
                long time = jResult.getTimestamp("date").getTime();
                entries.add(new Pointer(pointer, time));
            }
            jResult.close();

            ResultSet pResult = pointQuery.getResultSet();
            while (pResult.next()) {
                String category = pResult.getString("category");
                int count = pResult.getInt("count");
                points.add(new Point(category, count));
            }
            pResult.close();

            conn.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return new PlayerData(uuid, tags, entries, points, objectives);
    }

    public void clearPlayerDate(UUID uuid) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        try {
            Connection conn = dataManager.getConnection();
            Statement state = conn.createStatement();
            state.addBatch("DELETE FROM " + QuestTables.QUEST_OBJECTIVES.getTableName() + " WHERE uid = '" + uid + "'");
            state.addBatch("DELETE FROM " + QuestTables.QUEST_JOURNAL.getTableName() + " WHERE uid = '" + uid + "'");
            state.addBatch("DELETE FROM " + QuestTables.QUEST_POINTS.getTableName() + " WHERE uid = '" + uid + "'");
            state.addBatch("DELETE FROM " + QuestTables.QUEST_TAGS.getTableName() + " WHERE uid = '" + uid + "'");
            state.executeBatch();
            state.clearBatch();
            state.close();
            conn.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertJournal(UUID uuid, String pointer, String date) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeInsert(
                QuestTables.QUEST_JOURNAL.getTableName(),
                new String[] {"uid", "pointer", "date"},
                new Object[] {uid, pointer, date}
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

        try (DatabaseQuery query = dataManager.createQuery("SELECT * FROM " + QuestTables.QUEST_GLOBAL_TAGS.getTableName())) {
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

        try (DatabaseQuery query = dataManager.createQuery("SELECT * FROM " + QuestTables.QUEST_GLOBAL_POINTS.getTableName())) {
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
