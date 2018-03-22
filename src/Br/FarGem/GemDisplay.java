/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */

package Br.FarGem;

import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public interface GemDisplay {
    public Material getType(int lv);
    public short getDurability(int lv);
    public String getDisplayName(int lv);
    public List<String> getLore(int lv);
}
