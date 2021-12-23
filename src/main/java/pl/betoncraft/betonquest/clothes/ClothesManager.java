package pl.betoncraft.betonquest.clothes;

import lombok.Getter;
import org.bukkit.Bukkit;
import pl.betoncraft.betonquest.BetonQuest;

import java.util.HashMap;
import java.util.UUID;

public class ClothesManager {

    private HashMap<Integer, Merchant> merchantMap;

    @Getter
    private final HashMap<UUID, Integer> dialogue = new HashMap<>();

    public ClothesManager() {
        this.merchantMap = new HashMap<>();
    }

    public void init() {
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
