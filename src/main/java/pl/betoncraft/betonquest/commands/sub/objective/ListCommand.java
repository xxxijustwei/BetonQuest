package pl.betoncraft.betonquest.commands.sub.objective;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.utils.MessageUtils;

public class ListCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "list";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 1) return;

        String s1 = args[0];

        Player player = Bukkit.getPlayerExact(s1);
        if (player == null) {
            sender.sendMessage("§c Player's name is missing or he's offline");
            return;
        }

        MessageUtils.sendMessage(sender, "player_objectives");
        for (Objective obj : BetonQuest.getQuestManager().getPlayerObjectives(player.getUniqueId())) {
            sender.sendMessage("§b- " + obj.getLabel());
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
