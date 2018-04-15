/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public interface OptionalGemDisplay extends GemDisplay {

    @Override
    public default String getDisplayName(int lv) {
        if (!(this instanceof Gem)) {
            throw new IllegalStateException("实现接口的不是Gem类");
        }
        Gem gem = (Gem) this;
        return gem.getDisplayName();
    }

    @Override
    public default int getDurability(int lv) {
        return 0;
    }

    @Override
    public default List<String> getLore(int lv) {
        return new ArrayList<>();
    }

    @Override
    public default Material getType(int lv) {
        return Data.DefaultGemMaterial;
    }
    
    

}
