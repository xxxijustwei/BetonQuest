package pl.betoncraft.betonquest.clothes;

import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.sakuragame.eternal.dragoncore.api.ArmourAPI;
import org.bukkit.Bukkit;
import pl.betoncraft.betonquest.BetonQuest;

import java.util.HashMap;
import java.util.UUID;

public class ClothesManager {

    private HashMap<Integer, Merchant> merchantMap;
    @Getter private final HashMap<UUID, Integer> tryMap;

    @Getter
    private final HashMap<UUID, Integer> dialogue = new HashMap<>();

    public ClothesManager() {
        this.merchantMap = new HashMap<>();
        this.tryMap = new HashMap<>();
    }

    public void init() {
        this.merchantMap = BetonQuest.getFileManager().getMerchant();
        this.applyNPCSkin();
        Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §6§lSuccessfully loaded " + merchantMap.size() + " clothes npc.");
    }

    private void applyNPCSkin() {
        for (int key : merchantMap.keySet()) {
            NPC npc = CitizensAPI.getNPCRegistry().getById(key);
            if (npc == null) continue;

            Merchant merchant = merchantMap.get(key);
            ArmourAPI.setEntitySkin(npc.getEntity().getUniqueId(), merchant.getSkins());
        }
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
