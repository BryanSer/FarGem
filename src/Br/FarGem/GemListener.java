/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem;

import Br.API.Utils;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public class GemListener implements Listener {

    private static Map<String, Tools.GemInfo> TempDatas = new HashMap<>();

    public static boolean isInstalling(Player p) {
        return TempDatas.containsKey(p.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent evt) {
        if (TempDatas.containsKey(evt.getPlayer().getName())) {
            Tools.GemInfo td = TempDatas.get(evt.getPlayer().getName());
            evt.getPlayer().getInventory().addItem(new ItemStack[]{td.getGem().getGem(td.getLevel())});
            GemListener.TempDatas.remove(evt.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInstall(PlayerInteractEvent evt) {
        if (!TempDatas.containsKey(evt.getPlayer().getName())) {
            return;
        }
        if (!evt.hasItem()) {
            return;
        }
        if (this.hasItemOff(evt.getPlayer())) {
            return;
        }
        evt.setCancelled(true);
        Tools.GemInfo gi = TempDatas.get(evt.getPlayer().getName());
        TempDatas.remove(evt.getPlayer().getName());
        evt.setCancelled(true);
        ItemStack is = evt.getItem();
        if (is.getAmount() != 1) {
            evt.getPlayer().sendMessage("§c你只能给一个物品镶嵌");
            Utils.safeGiveItem(evt.getPlayer(), gi.getGem().getGem(gi.getLevel()));
            return;
        }
        if (!gi.getGem().canInstall(is)) {
            evt.getPlayer().sendMessage("§c这个物品不可镶嵌这个宝石");
            Utils.safeGiveItem(evt.getPlayer(), gi.getGem().getGem(gi.getLevel()));
            return;
        }
        ItemStack result = gi.getGem().Install(is, gi.getLevel());
        if (result == null) {
            evt.getPlayer().sendMessage("§c安装失败 请检查该物品是否已达宝石安装上限");
            Utils.safeGiveItem(evt.getPlayer(), gi.getGem().getGem(gi.getLevel()));
            return;
        }
        evt.getPlayer().getInventory().setItemInMainHand(result);
        evt.getPlayer().sendMessage("§6已成功镶嵌" + gi.getGem().getDisplayName());
    }

    @EventHandler
    public void onRightClickGem(PlayerInteractEvent evt) {
        if (TempDatas.containsKey(evt.getPlayer().getName())) {
            return;
        }
        if (!evt.hasItem()) {
            return;
        }
        if (evt.getAction().name().contains("LEFT")) {
            ItemStack result = Tools.updateItem(evt.getItem());
            if (result != null) {
                evt.setCancelled(true);
                evt.getPlayer().setItemInHand(result);
            }
            return;
        }
        ItemStack is = evt.getItem().clone();
        if (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName() || !is.getItemMeta().hasLore()) {
            return;
        }
        Tools.GemInfo gi = Tools.getGemInfo(is);
        if (gi == null) {
            return;
        }
        if (this.hasItemOff(evt.getPlayer())) {
            evt.getPlayer().sendMessage("§6镶嵌宝石的时候 副手不能有东西哦");
            return;
        }
        evt.setCancelled(true);
        evt.getPlayer().sendMessage("§6请再右键你要镶嵌的武器");
        TempDatas.put(evt.getPlayer().getName(), gi);
        if (is.getAmount() == 1) {
            evt.getPlayer().getInventory().setItemInMainHand(null);
        } else {
            is.setAmount(is.getAmount() - 1);
            evt.getPlayer().getInventory().setItemInMainHand(is);
        }
    }

    public boolean hasItemOff(Player p) {
        try {
            return !(p.getInventory().getItemInOffHand() == null || p.getInventory().getItemInOffHand().getType() == Material.AIR || p.getInventory().getItemInOffHand().getAmount() == 0);
        } catch (Throwable e) {
            return p.getItemInHand() != null;
        }
    }
}
