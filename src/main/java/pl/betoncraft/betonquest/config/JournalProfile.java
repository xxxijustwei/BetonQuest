package pl.betoncraft.betonquest.config;

import lombok.Getter;

import java.util.List;

@Getter
public class JournalProfile {

    private final String title;
    private final List<String> contents;

    public JournalProfile(String title, List<String> contents) {
        this.title = title;
        this.contents = contents;
    }
}
