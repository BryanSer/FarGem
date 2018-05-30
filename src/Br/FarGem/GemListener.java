package Br.FarGem;

/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
import Br.API.Utils;
import Br.FarGem.Tools;
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

    @Deprecated
    public static boolean isInstalling(Player p) {
        return InstallManager.InstallListener.isInstalling(p);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInt(PlayerInteractEvent evt) {
        if (evt.hasItem()) {
            ItemStack is;
            try {
                is = evt.getPlayer().getEquipment().getItemInMainHand();
            } catch (Throwable e) {
                is = evt.getPlayer().getItemInHand();
            }
            if (is != null) {
                is = is.clone();
                is = Tools.updateItem(is);
                if (is != null) {
                    try {
                        evt.getPlayer().getEquipment().setItemInMainHand(is);
                    } catch (Exception e) {
                        evt.getPlayer().setItemInHand(is);
                    }
                }
            }
        }
    }
}
