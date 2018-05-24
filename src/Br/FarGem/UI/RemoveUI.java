/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem.UI;

import Br.API.GUI.Ex.BaseUI;
import Br.API.GUI.Ex.Item;
import Br.API.GUI.Ex.Snapshot;
import Br.API.GUI.Ex.SnapshotFactory;
import Br.API.GUI.Ex.UIManager;
import Br.API.ItemBuilder;
import Br.API.Utils;
import Br.FarGem.Data;
import Br.FarGem.Gem;
import Br.FarGem.Remover.Type;
import Br.FarGem.Tools;
import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public class RemoveUI extends BaseUI {
    
    public static void RegisterUI(){
        UIManager.RegisterUI(new RemoveUI());
    }

    protected SnapshotFactory<RemoveUI> Factory;

    private Item[] Contains = new Item[18];

    public RemoveUI() {
        super.Name = "FG_RU";
        super.DisplayName = "§6宝石移除";
        super.Rows = 2;
        this.Factory = SnapshotFactory.getDefaultSnapshotFactory(this);

        for (int i = 0; i < Contains.length; i++) {//这个循环是把UI里全部填入一个基底 然后我们再修改
            Contains[i] = Item.getNewInstance(ItemBuilder.getBuilder(Material.STAINED_GLASS_PANE).name(" ").build());
        }

        Contains[3] = Item.getNewInstance((ItemStack) null)//玩家放武器用的格子
                .setButtonCellback(p -> true)
                .setUpadteDisplayLambda((p, s) -> s.getInventory().getItem(3))
                .setUpdate(true);
        Contains[5] = Item.getNewInstance((ItemStack) null)//玩家放卸除器用的格子
                .setButtonCellback(p -> true)
                .setUpadteDisplayLambda((p, s) -> s.getInventory().getItem(5))
                .setUpdate(true);

        for (int i = 10; i <= 16; i++) {
            final int slot = i - 10;
            Contains[i] = Item.getNewInstance((ItemStack) null)
                    .setUpadteDisplayLambda((p, s) -> {
                        s.removeData(String.valueOf(slot));
                        ItemStack is = s.getInventory().getItem(3);
                        if (is != null) {
                            List<Tools.GemInfo> gems = Tools.getInstalledGemAndLevel(is);
                            if (gems.size() <= slot) {
                                return null;
                            }
                            Tools.GemInfo g = gems.get(slot);//这个格子表示的宝石
                            s.setData(String.valueOf(slot), g);//这个是API里比较机智的一个方法 把临时数据写入快照
                            return g.getGem().getGem(g.getLevel());//返回宝石
                        }
                        return null;
                    })
                    .setClick(ClickType.LEFT, p -> {
                        Snapshot s = this.getSnapshot(p);
                        Object obj = s.getData(String.valueOf(slot));
                        if (obj != null) { //如果玩家点击的格子里有数据
                            Tools.GemInfo info = (Tools.GemInfo) obj;
                            ItemStack is = s.getInventory().getItem(5);//移除工具
                            if (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) {
                                return;
                            }
                            String d = is.getItemMeta().getDisplayName();
                            for (Type t : Type.values()) {//移除的种类枚举
                                if (Tools.decodeColorCode(d).contains(t.getID())) {//说明是移除工具 并且类型是t
                                    if (is.getAmount() == 1) {
                                        s.getInventory().setItem(5, null);
                                    } else {
                                        is = is.clone();
                                        is.setAmount(is.getAmount() - 1);
                                        s.getInventory().setItem(5, is);
                                    }
                                    ItemStack item = s.getInventory().getItem(3).clone();
                                    ItemMeta im = item.getItemMeta();
                                    List<String> lore = im.getLore();
                                    int remove = -1;
                                    for (int j = 0; j < lore.size(); j++) {
                                        String str = Tools.getIdentifier(lore.get(j));
                                        if (str != null) {
                                            String g[] = str.split("\\|");
                                            if (g[0].equals(String.valueOf(info.getGem().getIdentifier()))) {
                                                remove = j;
                                                break;
                                            }
                                        }
                                    }
                                    if (remove != -1) {
                                        lore.remove(remove);
                                        im.setLore(lore);
                                        is.setItemMeta(im);
                                        ItemStack tar = info.getGem().BeforeUninstall(is, info.getLevel());
                                        s.getInventory().setItem(3, tar);
                                        if (t == Type.Uninstall) {
                                            ItemStack gem = info.getGem().getGem(info.getLevel());
                                            Utils.safeGiveItem(p, gem);
                                        }
                                        p.sendMessage("§6卸除已完成");
                                    }
                                    return;
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public Item getItem(Player p, int slot) {
        return Contains[slot];
    }

    @Override
    public SnapshotFactory getSnapshotFactory() {
        return this.Factory;
    }

}
