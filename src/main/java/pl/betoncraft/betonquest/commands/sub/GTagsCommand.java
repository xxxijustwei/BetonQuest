package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubTabCompleter;
import pl.betoncraft.betonquest.commands.CommandPerms;
import pl.betoncraft.betonquest.commands.sub.globaltags.*;

public class GTagsCommand extends SubTabCompleter {

    public GTagsCommand() {
        super(new HelpCommand());
        register(new ListCommand());
        register(new PurgeCommand());
        register(new AddCommand());
        register(new DelCommand());
    }

    @Override
    public String getIdentifier() {
        return "gTags";
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
