/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem;

import Br.API.Calculator;
import Br.API.GUI.Item;
import Br.API.GUI.Menu;
import Br.API.GUI.MenuManager;
import Br.API.ItemBuilder;
import Br.API.Utils;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public class Craft {

    /**
     * amount, lv, chanse
     */
    public static BiFunction<Integer, Integer, Double> DefaultChance;
    public static Map<String, BiFunction<Integer, Integer, Double>> Chance = new HashMap<>();
    public static Map<ItemStack, Double> Stabilizer = new HashMap<>();
    private static int Minimal;
    private static int MaxLevel;
    
    public static void init() {
        File f = new File(Data.Plugin.getDataFolder(), "craft.yml");
        if (!f.exists()) {
            try {
                Utils.OutputFile(Data.Plugin, "craft.yml", null);
            } catch (IOException ex) {
                Logger.getLogger(Craft.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        if (!config.getBoolean("Enable")) {
            return;
        }
        Craft.Minimal = config.getInt("Minimal");
        Craft.MaxLevel = config.getInt("MaxLevel");
        try {
            ConfigurationSection cs = config.getConfigurationSection("Chance");
            for (String key : cs.getKeys(false)) {
                String s = cs.getString(key);
                Chance.put(key, (a, lv) -> Cal(a, lv, s));
            }
        } catch (Throwable e) {
        }
        Craft.DefaultChance = (a, lv) -> {
            try {
                return config.getDouble("DefaultChance." + lv + "." + a);
            } catch (Throwable e) {
                return 0d;
            }
        };
        for (String s : config.getStringList("Stabilizer")) {
            String v[] = s.split("\\|", 2);
            Stabilizer.put(Utils.AnalyticalItem_2(v[1]), Double.parseDouble(v[0]));
        }
        CreateUI();
    }
    
    private static DecimalFormat DF = new DecimalFormat("##%");
    
    public static void CreateUI() {
        MenuManager.RegisterMenu(Menu.getBuilder()
                .setName("FarCraft")
                .setSize(1)
                .setDisplayname("§6宝石合成")
                .fillItem(Item.getItemBuilder()
                        .setDisplay(ItemBuilder.getBuilder(Material.STAINED_GLASS_PANE).name("").build())
                        .build(), 9)
                .setItem(4, Item.getItemBuilder()
                        .setDisplayMethod((p) -> {
                            ItemStack is = p.getInventory().getItemInMainHand().clone();
                            int amount = is.getAmount();
                            Tools.GemInfo info = Tools.getGemInfo(is);
                            if (info == null || info.getLevel() == info.getGem().getMaxLevel() || info.getLevel() >= Craft.MaxLevel) {
                                return ItemBuilder.getBuilder(Material.BARRIER)
                                        .name("§c无法合成")
                                        .build();
                            }
                            double chance;
                            if (Craft.Chance.containsKey(info.getGem().getName())) {
                                chance = Craft.Chance.get(info.getGem().getName()).apply(amount, info.getLevel());
                            } else {
                                chance = Craft.DefaultChance.apply(amount, info.getLevel());
                            }
                            double w = 0d;
                            for (ItemStack item : p.getInventory()) {
                                if (item == null) {
                                    continue;
                                }
                                ItemStack itemo = item.clone();
                                itemo.setAmount(1);
                                Double d = Stabilizer.get(itemo);
                                if (d != null) {
                                    w += d * item.getAmount();
                                }
                            }
                            return ItemBuilder.getBuilder(Material.STAINED_GLASS_PANE)
                                    .durability((short) 14)
                                    .name("§b点击合成手上宝石")
                                    .lore("§7合成概率: §" + (chance + w > 0.6 ? 'a' : 'c') + DF.format(chance) + " + " + DF.format(w),
                                            "§7合成数量: " + (amount >= Craft.Minimal ? "§a达标" : "§c不达标"),
                                            "§7稳定剂: §c" + (w == 0d ? "无" : "+" + DF.format(w)),//TODO
                                            "§7保护石: §c无")
                                    .build();
                        })
                        .setUse((p) -> {
                            ItemStack is = p.getInventory().getItemInMainHand().clone();
                            Tools.GemInfo info = Tools.getGemInfo(is);
                            if (info == null || info.getLevel() == info.getGem().getMaxLevel() || info.getLevel() >= Craft.MaxLevel) {
                                p.sendMessage("§c不是宝石或宝石已满级");
                                return;
                            }
                            int amount = is.getAmount();
                            if (amount < Craft.Minimal) {
                                p.sendMessage("§c宝石不足");
                                return;
                            }
                            double chance;
                            if (Craft.Chance.containsKey(info.getGem().getName())) {
                                chance = Craft.Chance.get(info.getGem().getName()).apply(amount, info.getLevel());
                            } else {
                                chance = Craft.DefaultChance.apply(amount, info.getLevel());
                            }
                            p.getInventory().setItemInMainHand(null);
                            double w = 0d;
                            ItemStack[] items = p.getInventory().getContents();
                            for (int i = 0; i < items.length; i++) {
                                ItemStack item = items[i];
                                if (item == null) {
                                    continue;
                                }
                                ItemStack itemo = item.clone();
                                itemo.setAmount(1);
                                Double d = Stabilizer.get(itemo);
                                if (d != null) {
                                    w += d * item.getAmount();
                                    items[i] = null;
                                }
                            }
                            p.getInventory().setContents(items);
                            if (Math.random() < chance + w) {
                                p.sendMessage("§6合成成功");
                                ItemStack item = info.getGem().getGem(info.getLevel() + 1);
                                Utils.safeGiveItem(p, item);
                            } else {
                                p.sendMessage("§c合成失败");
                            }
                        })
                        .build())
                .build());
    }
    
    public static double Cal(int a, int lv, String c) {
        return Calculator.conversion(c.replaceAll("amount", String.valueOf(a)).replaceAll("lv", String.valueOf(lv)));
    }
}
