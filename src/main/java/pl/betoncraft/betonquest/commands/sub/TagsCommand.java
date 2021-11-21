package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubTabCompleter;
import pl.betoncraft.betonquest.commands.sub.tags.AddCommand;
import pl.betoncraft.betonquest.commands.sub.tags.DelCommand;
import pl.betoncraft.betonquest.commands.sub.tags.HelpCommand;
import pl.betoncraft.betonquest.commands.sub.tags.ListCommand;

public class TagsCommand extends SubTabCompleter {

    public TagsCommand() {
        super(new HelpCommand());
        register(new ListCommand());
        register(new AddCommand());
        register(new DelCommand());
    }

    @Override
    public String getIdentifier() {
        return "tags";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String getPermission() {
        return null;
    }
}
