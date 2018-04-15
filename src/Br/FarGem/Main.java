/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem;

import Br.API.Log;
import Br.API.Metrics;
import Br.API.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public class Main extends JavaPlugin {

    public static Log log;

    @Override
    public void onEnable() {
        Data.init(this);
        Scripts.LoadScripts(this);
        Bukkit.getPluginManager().registerEvents(new GemListener(), this);
        Main.log = Br.API.Log.CombineOldLog(this, 1);
        Bukkit.getPluginManager().registerEvents(new Remover(), this);
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("gems", () -> {
            return Data.GemIDMap.size();
        }));
        metrics.addCustomChart(new Metrics.SingleLineChart("scriptsgems", () -> {
            return Scripts.LoadFromScripts.size();
        }));
    }

    @Override
    public void onDisable() {
        log.Save();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            return false;
        }
        if (args[0].equalsIgnoreCase("give") && sender.isOp()) {
            if (args.length < 4) {
                sender.sendMessage("§c参数不足");
                return true;
            }
            Player o = Bukkit.getPlayerExact(args[1]);
            if (o == null || !o.hasPlayedBefore() || !o.isOnline()) {
                sender.sendMessage("找不到玩家");
                return true;
            }
            if (args[2].equalsIgnoreCase("remove")) {
                ItemStack is = Remover.getRemover();
                Utils.safeGiveItem(o, is);
                o.getPlayer().sendMessage("§6一个宝石移除器已放入你的背包");
                Main.log.Log(o.getName() + " -> Removeer   操作者: " + sender.getName());
                sender.sendMessage("§6发送成功");
                return true;
            }
            if (args[2].equalsIgnoreCase("uninstaller")) {
                ItemStack is = Remover.getUninstaller();
                Utils.safeGiveItem(o, is);
                o.getPlayer().sendMessage("§6一个宝石移除器已放入你的背包");
                Main.log.Log(o.getName() + " -> Uninstaller   操作者: " + sender.getName());
                sender.sendMessage("§6发送成功");
                return true;
            }
            Gem g = Data.getGem(args[2]);
            if (g == null) {
                sender.sendMessage("§c找不到宝石");
                return true;
            }
            try {
                int lv = Integer.parseInt(args[3]);
                if (lv > g.getMaxLevel() || lv <= 0) {
                    sender.sendMessage("§c等级过大或过低");
                    return true;
                }
                ItemStack is = g.getGem(lv);
                Utils.safeGiveItem(o, is);
                o.getPlayer().sendMessage("§6一个宝石已放入你的背包");
                Main.log.Log(o.getName() + " -> " + g.getName() + " * " + lv + "   操作者: " + sender.getName());
                sender.sendMessage("§6发送成功");
            } catch (NumberFormatException n) {
            }
        }
        if (args[0].equalsIgnoreCase("reload") && sender.isOp()) {
            Data.init(this);
            Scripts.LoadScripts(this);
            for (Gem g : Data.GemIDMap.values()) {
                Data.LoadGemData(g);
            }
            sender.sendMessage("§6重载完成");
            return true;
        }
        if (args[0].equalsIgnoreCase("all") && (sender instanceof Player) && sender.isOp()) {
            Player p = (Player) sender;
            for (Gem g : Data.GemIDMap.values()) {
                ItemStack[] is = new ItemStack[g.getMaxLevel()];
                for (int i = 0; i < is.length; i++) {
                    is[i] = g.getGem(i + 1);
                }
                for (ItemStack i : is) {
                    Utils.safeGiveItem(p, i);
                }
            }
            sender.sendMessage("§6宝石已发送到背包");
            return true;
        }
        return true;
    }

}
