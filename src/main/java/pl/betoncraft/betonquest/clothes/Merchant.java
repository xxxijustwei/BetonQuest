package pl.betoncraft.betonquest.clothes;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

@Getter
@AllArgsConstructor
public class Merchant {

    private final String name;
    private final List<String> response;
    private final List<String> skin;
    private final Double price;

    public Merchant(ConfigurationSection section) {
        this.name = MegumiUtil.onReplace(section.getString("name"));
        this.response = MegumiUtil.onReplace(section.getStringList("response"));
        this.skin = section.getStringList("skin");
        this.price = section.getDouble("price");
    }

}
