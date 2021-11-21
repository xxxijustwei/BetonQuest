package pl.betoncraft.betonquest.storage;

import net.sakuragame.eternal.dragoncore.database.mysql.DatabaseTable;
import pl.betoncraft.betonquest.config.FileManager;

public enum QuestTables {

    QUEST_OBJECTIVES(new DatabaseTable(FileManager.getTablePrefix() + "objectives",
            new String[] {
                    "`id` int AUTO_INCREMENT",
                    "`uid` int NOT NULL",
                    "`objective` varchar(64) NOT NULL",
                    "`instructions` text NOT NULL",
                    "UNIQUE KEY `account` (`id`,`uid`)"
            })),
    QUEST_TAGS(new DatabaseTable(FileManager.getTablePrefix() + "tags",
            new String[] {
                    "`id` int AUTO_INCREMENT",
                    "`uid` int NOT NULL",
                    "`tag` TEXT NOT NULL",
                    "UNIQUE KEY `account` (`id`,`uid`)"
            })),
    QUEST_POINTS(new DatabaseTable(FileManager.getTablePrefix() + "points",
            new String[] {
                    "`id` int AUTO_INCREMENT",
                    "`uid` int NOT NULL",
                    "`category` VARCHAR(64)",
                    "`count` int NOT NULL",
                    "UNIQUE KEY `account` (`id`,`uid`)"
            })),
    QUEST_JOURNAL(new DatabaseTable(FileManager.getTablePrefix() + "journal",
            new String[] {
                    "`id` int AUTO_INCREMENT",
                    "`uid` int NOT NULL",
                    "`pointer` varchar(64) NOT NULL",
                    "`date` timestamp NOT NULL",
                    "UNIQUE KEY `account` (`id`,`uid`)"
            })),
    QUEST_GLOBAL_TAGS(new DatabaseTable(FileManager.getTablePrefix() + "global_tags",
            new String[] {
                    "`id` int PRIMARY KEY AUTO_INCREMENT",
                    "`tag` TEXT NOT NULL"
            })),
    QUEST_GLOBAL_POINTS(new DatabaseTable(FileManager.getTablePrefix() + "global_points",
            new String[] {
                    "`id` int AUTO_INCREMENT",
                    "`category` varchar(64) NOT NULL",
                    "`count` int NOT NULL",
                    "UNIQUE KEY `account` (`id`,`category`)"
            }));

    private final DatabaseTable table;

    QuestTables(DatabaseTable table) {
        this.table = table;
    }

    public String getTableName() {
        return table.getTableName();
    }

    public String[] getColumns() {
        return table.getTableColumns();
    }

    public DatabaseTable getTable() {
        return table;
    }

    public void createTable() {
        table.createTable();
    }
}
