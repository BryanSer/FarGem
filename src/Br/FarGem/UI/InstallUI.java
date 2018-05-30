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
import Br.API.GUI.MenuManager;
import Br.API.ItemBuilder;
import Br.API.Utils;
import Br.FarGem.Gem;
import Br.FarGem.Tools;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public class InstallUI extends BaseUI {

    protected SnapshotFactory Factory;

    public static void RegisterUI() {
        UIManager.RegisterUI(new InstallUI());
    }

    Item Contains[] = new Item[9];
// 1 0 1 0 1 2 3 1 1

    private static final int ITEM_SLOT = 1;
    private static final int GEM_SLOT = 3;
    private static final int RESULT_SLOT = 6;

    public InstallUI() {
        super.Name = "FG_IU";
        super.DisplayName = "§b宝石镶嵌";
        super.Rows = 1;
        this.Factory = SnapshotFactory.getDefaultSnapshotFactory(this);

        Contains[0] = Item.getNewInstance(ItemBuilder.getBuilder(Material.STAINED_GLASS_PANE).name("  ").durability((short) 1).build());
        Contains[8] = Item.getNewInstance(ItemBuilder.getBuilder(Material.ENCHANTMENT_TABLE)
                .name("§b点击打开宝石合成菜单")
                .build())
                .setClick(ClickType.LEFT, (p) -> MenuManager.OpenMenuDelay(p, "FarCraft"));
        Contains[7] = Item.getNewInstance(ItemBuilder.getBuilder(Material.IRON_PICKAXE)
                .name("§b点击打开宝石卸除菜单")
                .build())
                .setClick(ClickType.LEFT, (p) -> UIManager.OpenUI(p, "FG_RU"));
        Contains[2] = Contains[4] = Item.getNewInstance(ItemBuilder.getBuilder(Material.STAINED_GLASS_PANE).name("  ").durability((short) 4).build());
        Contains[ITEM_SLOT] = Item.getNewInstance((ItemStack) null)
                .setButtonCellback(p -> true)
                .setUpadteDisplayLambda((p, s) -> {
                    Inventory inv = s.getInventory();
                    return inv.getItem(ITEM_SLOT);
                })
                .setUpdate(true);
        Contains[GEM_SLOT] = Item.getNewInstance((ItemStack) null)
                .setButtonCellback(p -> true)
                .setUpadteDisplayLambda((p, s) -> {
                    Inventory inv = s.getInventory();
                    return inv.getItem(GEM_SLOT);
                })
                .setUpdate(true);
        Contains[5] = Item.getNewInstance(ItemBuilder.getBuilder(Material.EMERALD).name("§b点击镶嵌").build())
                .setClick(ClickType.LEFT, (p) -> {
                    Snapshot s = super.getSnapshot(p);
                    Inventory inv = s.getInventory();
                    ItemStack item = inv.getItem(ITEM_SLOT);
                    if (item == null) {
                        return;
                    }
                    ItemStack gem = inv.getItem(GEM_SLOT);
                    Tools.GemInfo gi = Tools.getGemInfo(gem);
                    if (gi == null) {
                        return;
                    }
                    Gem g = gi.getGem();
                    if (!g.canInstall(item)) {
                        return;
                    }
                    if (item.getAmount() != 1 || gem.getAmount() != 1) {
                        p.sendMessage("§c参与镶嵌物品过多");
                        return;
                    }
                    ItemStack is = g.Install(item.clone(), gi.getLevel());
                    is = g.BeforeInstall(is, gi.getLevel());
                    if (is != null) {
                        ItemStack iss = Tools.updateItem(is);
                        inv.setItem(RESULT_SLOT, iss == null ? is : iss);
                        inv.setItem(GEM_SLOT, null);
                        inv.setItem(ITEM_SLOT, null);
                    } else {
                        p.sendMessage("§c安装失败 请检查该物品是否已达宝石安装上限");
                    }
                });
        Contains[RESULT_SLOT] = Item.getNewInstance((t) -> null).setUpadteDisplayLambda((p, s) -> {
            Inventory inv = s.getInventory();
            return inv.getItem(RESULT_SLOT);
        }).setButtonCellback(p -> true);
    }

    @Override
    public Item getItem(Player p, int slot) {
        return Contains[slot];
    }

    @Override
    public SnapshotFactory getSnapshotFactory() {
        return this.Factory;
    }

    @Override
    public void onClose(Player p, Snapshot s) {//1 3 6
        Inventory inv = s.getInventory();
        Utils.safeGiveItem(p, inv.getItem(ITEM_SLOT));
        Utils.safeGiveItem(p, inv.getItem(GEM_SLOT));
        Utils.safeGiveItem(p, inv.getItem(RESULT_SLOT));
    }

}
