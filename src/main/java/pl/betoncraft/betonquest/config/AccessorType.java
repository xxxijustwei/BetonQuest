package pl.betoncraft.betonquest.config;

public enum AccessorType {
    MAIN("main", "main.yml"),
    EVENTS("event", "events.yml"),
    CONDITIONS("condition", "conditions.yml"),
    OBJECTIVES("objective", "objectives.yml"),
    JOURNAL("journal", "journal.yml"),
    CONVERSATION("conversation", "conversation.yml"),
    CUSTOM("custom", "custom.yml"),
    OTHER("", "");

    private final String folder;
    private final String fileName;

    AccessorType(String folder, String resource) {
        this.folder = folder;
        this.fileName = resource;
    }

    public String getFolder() {
        return folder;
    }

    public String getResource() {
        return "setting/" + fileName;
    }

    public String getPath() {
        if (this == MAIN || this == CUSTOM) return folder + ".yml";
        return folder + "/" + folder + ".yml";
    }
}