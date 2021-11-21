package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.commands.CommandPerms;
import pl.betoncraft.betonquest.core.id.EventID;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.MessageUtils;

import java.util.UUID;
import java.util.logging.Level;

public class EventCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "event";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) return;

        String s1 = args[0];
        String s2 = args[1];

        Player player = Bukkit.getPlayerExact(s1);

        if (player == null) {
            sender.sendMessage("§c Player's name is missing or he's offline");
            return;
        }

        UUID uuid = player.getUniqueId();
        try {
            EventID event = new EventID(s2);
            QuestManager.event(uuid, event);
            MessageUtils.sendMessage(sender, "player_event", new String[] {event.generateInstruction().getInstruction()});
        }
        catch (ObjectNotFoundException e) {
            MessageUtils.sendMessage(sender, "error", new String[]{e.getMessage()});
            LogUtils.getLogger().log(Level.WARNING, "Could not find event: " + e.getMessage());
            LogUtils.logThrowable(e);
        }
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
