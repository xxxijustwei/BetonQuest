package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubTabCompleter;
import pl.betoncraft.betonquest.commands.CommandPerms;
import pl.betoncraft.betonquest.commands.sub.objective.*;
import pl.betoncraft.betonquest.commands.sub.objective.HelpCommand;

public class ObjectiveCommand extends SubTabCompleter {

    public ObjectiveCommand() {
        super(new HelpCommand());
        register(new ListCommand());
        register(new StartCommand());
        register(new DeleteCommand());
        register(new CompleteCommand());
    }

    @Override
    public String getIdentifier() {
        return "objective";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String getPermission() {
        return CommandPerms.ADMIN.getNode();
    }
}
