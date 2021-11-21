package pl.betoncraft.betonquest.commands;

import lombok.Getter;

public enum CommandPerms {

    USER("betonquest.user"),
    ADMIN("betonquest.admin");

    @Getter
    private final String node;

    CommandPerms(String node) {
        this.node = node;
    }
}
