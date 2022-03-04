package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubTabCompleter;
import pl.betoncraft.betonquest.commands.sub.journal.*;
import pl.betoncraft.betonquest.commands.sub.journal.HelpCommand;

public class JournalCommand extends SubTabCompleter {

    public JournalCommand() {
        super(new HelpCommand());
        register(new OpenCommand());
        register(new ListCommand());
        register(new AddCommand());
        register(new DelCommand());
    }

    @Override
    public String getIdentifier() {
        return "journal";
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
