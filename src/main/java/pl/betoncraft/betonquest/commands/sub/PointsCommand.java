package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubTabCompleter;
import pl.betoncraft.betonquest.commands.CommandPerms;
import pl.betoncraft.betonquest.commands.sub.points.AddCommand;
import pl.betoncraft.betonquest.commands.sub.points.DelCommand;
import pl.betoncraft.betonquest.commands.sub.points.HelpCommand;
import pl.betoncraft.betonquest.commands.sub.points.ListCommand;

public class PointsCommand extends SubTabCompleter {

    public PointsCommand() {
        super(new HelpCommand());
        register(new ListCommand());
        register(new AddCommand());
        register(new DelCommand());
    }

    @Override
    public String getIdentifier() {
        return "points";
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
