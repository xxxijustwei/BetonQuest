package pl.betoncraft.betonquest.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.QuestDataUpdateEvent;
import pl.betoncraft.betonquest.utils.Scheduler;

import java.util.UUID;

public class QuestListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onDataUpdate(QuestDataUpdateEvent e) {
        UUID uuid = e.getUUID();
        String objective = e.getObjID();
        String date = e.getData();
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().updateObjective(uuid, objective, date));
    }
}
