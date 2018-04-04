/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem;

import Br.API.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public class Remover implements Listener {

    public enum Type {
        Remove("remove"),
        Uninstall("uninstall");
        private String ID;

        private Type(String k) {
            this.ID = k;
        }

        public String getID() {
            return ID;
        }
    }

    public static ItemStack getRemover() {
        ItemStack is = new ItemStack(Material.STICK);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Tools.encodeColorCode(Type.Remove.getID()) + "§b宝石移除工具");
        im.setLore(Arrays.asList("§6右键使用", "§c注意 将直接删除宝石"));
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack getUninstaller() {
        ItemStack is = new ItemStack(Material.STICK);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Tools.encodeColorCode(Type.Uninstall.getID()) + "§b宝石卸除工具");
        im.setLore(Arrays.asList("§6右键使用", "§b宝石将会完好的卸回背包"));
        is.setItemMeta(im);
        return is;
    }

    private Map<String, Type> WaitForRemove = new HashMap<>();

    private Map<String, ItemStack> Queue = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onClickEquip(PlayerInteractEvent evt) {
        if(Queue.containsKey(evt.getPlayer().getName())){
            evt.getPlayer().sendMessage("§6请先在聊天框中选择你要卸除的宝石");
            evt.setCancelled(true);
            return;
        }
        if (!evt.hasItem()) {
            return;
        }
        Type t = WaitForRemove.get(evt.getPlayer().getName());
        if (t == null) {
            return;
        }
        ItemStack is = evt.getItem();
        Set<Gem> gems = Tools.getInstalledGem(is);
        boolean isEQ = is.hasItemMeta() && is.getItemMeta().hasLore() && gems != null && !gems.isEmpty();
        isEQ = isEQ ^ Utils.hasItemInOffHand(evt.getPlayer()) && isEQ;
        if (!isEQ) {
            evt.getPlayer().sendMessage("§c你手上的东西没有镶嵌任何宝石或你副手有东西");
            Utils.safeGiveItem(evt.getPlayer(), t == Type.Remove ? getRemover() : getUninstaller());
            WaitForRemove.remove(evt.getPlayer().getName());
            return;
        }
        this.Return(evt.getPlayer());
        String gemname[] = new String[gems.size()];
        String display[] = new String[gems.size()];
        int index = 0;
        for (Gem s : gems) {
            gemname[index] = s.getName();
            display[index++] = s.getDisplayName();
        }
        Queue.put(evt.getPlayer().getName(), evt.getItem().clone());
        evt.getPlayer().setItemInHand(null);
        Utils.CallBack.SendButtonRequest(evt.getPlayer(), display, (p, i) -> {
            if (i == null || i < 0 || i >= gemname.length) {
                Queue.remove(p.getName());
                return;
            }
            ItemStack item = Queue.remove(p.getName());
            ItemMeta im = item.getItemMeta();
            List<String> lore = im.getLore();
            int remove = -1;
            int lv = -1;
            String targem = gemname[i];
            Gem targ = Data.getGem(targem);
            for (int j = 0; j < lore.size(); j++) {
                String s = Tools.getIdentifier(lore.get(j));
                if (s != null) {
                    String g[] = s.split("\\|");
                    if (g[0].equals(String.valueOf(targ.getIdentifier()))) {
                        remove = j;
                        lv = Integer.parseInt(g[2]);
                        break;
                    }
                }
            }
            if (remove != -1) {
                lore.remove(remove);
                im.setLore(lore);
                is.setItemMeta(im);
                Utils.safeGiveItem(p, is);
                if (t == Type.Uninstall) {
                    ItemStack gem = targ.getGem(lv);
                    Utils.safeGiveItem(p, gem);
                }
                p.sendMessage("§6卸除已完成");
            }
            WaitForRemove.remove(p.getName());
        });
    }

    public void Return(Player p) {
        ItemStack is = Queue.remove(p.getName());
        if (is != null) {
            p.sendMessage("§6你之前的移除请求已经取消 物品已返还至背包");
            Utils.safeGiveItem(p, is);
            Type t = WaitForRemove.remove(p.getName());
            if (t != null) {
                Utils.safeGiveItem(p, t == Type.Remove ? getRemover() : getUninstaller());
            }
        }
    }

    @EventHandler
    public void onInt(PlayerInteractEvent evt) {
        if (!evt.hasItem()) {
            return;
        }
        if (WaitForRemove.containsKey(evt.getPlayer().getName())) {
            return;
        }
        ItemStack is = evt.getItem().clone();
        if (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) {
            return;
        }
        String d = is.getItemMeta().getDisplayName();
        for (Type t : Type.values()) {
            if (Tools.decodeColorCode(d).contains(t.getID())) {
                evt.setCancelled(true);
                if (Utils.hasItemInOffHand(evt.getPlayer())) {
                    evt.getPlayer().sendMessage("§6使用这个物品的时候副手不能有东西");
                    return;
                }
                WaitForRemove.put(evt.getPlayer().getName(), t);
                evt.getPlayer().sendMessage("§6请右键需要移除宝石的装备");
                if (is.getAmount() > 1) {
                    is.setAmount(is.getAmount() - 1);
                    evt.getPlayer().setItemInHand(is);
                } else {
                    evt.getPlayer().setItemInHand(null);
                }
                return;
            }
        }
    }
}
