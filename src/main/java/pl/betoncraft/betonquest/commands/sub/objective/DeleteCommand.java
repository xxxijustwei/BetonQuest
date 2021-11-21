package pl.betoncraft.betonquest.commands.sub.objective;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.core.id.ObjectiveID;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.MessageUtils;

import java.util.logging.Level;

public class DeleteCommand extends SubCommand {

    @Override
    public String getIdentifier() {
        return "delete";
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

        PlayerData data = BetonQuest.getInstance().getPlayerData(player.getUniqueId());

        try {
            ObjectiveID id = new ObjectiveID(s2);
            Objective objective = BetonQuest.getQuestManager().getObjective(id);
            if (objective == null) {
                MessageUtils.sendMessage(sender, "specify_objective");
                return;
            }

            objective.removePlayer(player.getUniqueId());
            data.removeRawObjective(id);
            MessageUtils.sendMessage(sender, "objective_removed");
        }
        catch (ObjectNotFoundException e) {
            MessageUtils.sendMessage(sender, "error", new String[]{e.getMessage()});
            LogUtils.getLogger().log(Level.WARNING, "Could not find objective: " + e.getMessage());
            LogUtils.logThrowable(e);
        }
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
