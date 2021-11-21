package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubTabCompleter;
import pl.betoncraft.betonquest.commands.sub.journal.AddCommand;
import pl.betoncraft.betonquest.commands.sub.journal.DelCommand;
import pl.betoncraft.betonquest.commands.sub.journal.HelpCommand;
import pl.betoncraft.betonquest.commands.sub.journal.ListCommand;

public class JournalCommand extends SubTabCompleter {

    public JournalCommand() {
        super(new HelpCommand());
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
