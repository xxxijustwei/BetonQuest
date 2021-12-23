package pl.betoncraft.betonquest.clothes;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import ink.ptms.zaphkiel.ZaphkielAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.sakuragame.eternal.gemseconomy.currency.EternalCurrency;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@AllArgsConstructor
public class Merchant {

    private final String name;
    private final List<String> response;
    private final List<String> skins;
    private final List<String> items;
    private final EternalCurrency currency;
    private final Double price;

    public Merchant(ConfigurationSection section) {
        this.name = MegumiUtil.onReplace(section.getString("name"));
        this.response = MegumiUtil.onReplace(section.getStringList("response"));
        this.skins = section.getStringList("skin");
        this.items = section.getStringList("item");
        this.currency = EternalCurrency.valueOf(section.getString("currency"));
        this.price = section.getDouble("price");
    }

    public int giveItem(Player player) {
        for (String key : items) {
            // TODO
            // if the inventory is full
            // send mail to player

            ItemStack item = ZaphkielAPI.INSTANCE.getItemStack(key, player);
            player.getInventory().addItem(item);
        }

        return 0;
    }

}
