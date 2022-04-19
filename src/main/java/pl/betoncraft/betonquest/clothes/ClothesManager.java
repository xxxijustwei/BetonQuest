package pl.betoncraft.betonquest.clothes;

import lombok.Getter;
import org.bukkit.Bukkit;
import pl.betoncraft.betonquest.BetonQuest;

import java.util.HashMap;
import java.util.UUID;

public class ClothesManager {

    private final BetonQuest plugin;
    private HashMap<Integer, Merchant> merchantMap;
    @Getter private final HashMap<UUID, Integer> tryMap;

    @Getter
    private final HashMap<UUID, Integer> dialogue = new HashMap<>();

    public ClothesManager(BetonQuest plugin) {
        this.plugin = plugin;
        this.merchantMap = new HashMap<>();
        this.tryMap = new HashMap<>();
    }

    public void init() {
        if (Bukkit.getPluginManager().getPlugin("Adyeshach") == null) {
            Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §6§lClothes module startup failure.(Need Adyeshach)");
            return;
        }

        this.plugin.registerListener(new ClothesListener());

        this.merchantMap = BetonQuest.getFileManager().getMerchant();

        Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §6§lSuccessfully loaded " + merchantMap.size() + " clothes npc.");
    }

    public Merchant getMerchant(int npcID) {
        return merchantMap.get(npcID);
    }

    public Integer getDialogueNPC(UUID uuid) {
        return dialogue.get(uuid);
    }

    public Merchant getDialogueMerchant(UUID uuid) {
        Integer npcID = getDialogueNPC(uuid);
        if (npcID == null) return null;

        return merchantMap.get(npcID);
    }
}
