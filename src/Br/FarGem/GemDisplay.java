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
 * 宝石的样式控制接口 非强制要求实现
 * @author Bryan_lzh
 * @version 1.0
 */
public interface GemDisplay {

    /**
     * 返回这个宝石的物品类型
     * @param lv
     * @return
     */
    public Material getType(int lv);

    /**
     * 返回宝石物品的损伤值
     * @param lv
     * @return
     */
    public int getDurability(int lv);

    /**
     * 返回宝石物品的显示名
     * @param lv
     * @return
     */
    public String getDisplayName(int lv);

    /**
     * 返回宝石物品的自定义Lore
     * @param lv
     * @return
     */
    public List<String> getLore(int lv);
}
