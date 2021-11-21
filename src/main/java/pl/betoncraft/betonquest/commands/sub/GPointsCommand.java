package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubTabCompleter;
import pl.betoncraft.betonquest.commands.CommandPerms;
import pl.betoncraft.betonquest.commands.sub.globalpoints.*;

public class GPointsCommand extends SubTabCompleter {

    public GPointsCommand() {
        super(new HelpCommand());
        register(new ListCommand());
        register(new PurgeCommand());
        register(new AddCommand());
        register(new DelCommand());
    }

    @Override
    public String getIdentifier() {
        return "gPoints";
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
