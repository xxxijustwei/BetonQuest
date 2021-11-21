package pl.betoncraft.betonquest.commands;

import com.taylorswiftcn.justwei.commands.JustCommand;
import pl.betoncraft.betonquest.commands.sub.*;

public class MainCommand extends JustCommand {

    public MainCommand() {
        super(new HelpCommand());
        register(new ConditionCommand());
        register(new EventCommand());
        register(new ObjectiveCommand());
        register(new GTagsCommand());
        register(new GPointsCommand());
        register(new TagsCommand());
        register(new PointsCommand());
        register(new JournalCommand());
        register(new PurgeCommand());
        register(new ReloadCommand());
    }
}
