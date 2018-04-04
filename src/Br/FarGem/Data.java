/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public class Data {

    private static Map<String, Integer> Identifiers = new HashMap<>();
    public static Map<Integer, Gem> GemIDMap = new HashMap<>();
    public static Main Plugin;

    public static String InstallPrefix_Value;

    /**
     * 通过名字寻找某宝石
     *
     * @param name
     * @return
     */
    public static Gem getGem(String name) {
        Integer id = Identifiers.get(name);
        if (id == null) {
            try {
                id = Integer.getInteger(name);
            } catch (NumberFormatException e) {
            }
        }
        return GemIDMap.get(id);
    }

    public static void init(Main p) {
        Data.Plugin = p;
        LoadConfig();
        LoadIdentifiers();
    }

    public static void LoadConfig() {
        if (!Plugin.getDataFolder().exists()) {
            Plugin.saveDefaultConfig();
        }
        FileConfiguration config = Plugin.getConfig();
        InstallPrefix_Value = ChatColor.translateAlternateColorCodes('&', config.getString("InstallPrefix.Value"));
    }

    /**
     * 加载宝石配置(建议不要使用 本框架会自动调用)
     *
     * @param g
     */
    public static void LoadGemData(Gem g) {
        if (g.getConfigs() == null) {
            return;
        }
        File file = Tools.getFile(Tools.FileType.GET_AND_CREATE, "Gems", g.getName() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        boolean edit = false;
        for (Gem.Config<Object> f : g.getConfigs()) {
            if (f.isNeedLevel()) {
                if (config.contains(f.getName() + ".1")) {
                    Object[] objs = new Object[g.getMaxLevel()];
                    for (int i = 1; i <= g.getMaxLevel(); i++) {
                        objs[i - 1] = config.get(f.getName() + "." + i);
                    }
                    f.setFunction((lv) -> {
                        return objs[lv - 1];
                    });
                } else {
                    edit = true;
                    for (int i = 1; i <= g.getMaxLevel(); i++) {
                        config.set(f.getName() + "." + i, f.getValue(i));
                    }
                }
            } else {
                if (config.contains(f.getName())) {
                    Object obj = config.get(f.getName());
                    f.setFunction((o) -> obj);
                } else {
                    edit = true;
                    config.set(f.getName(), f.getValue());
                }
            }
        }
        if (edit) {
            try {
                config.save(file);
            } catch (IOException ex) {
                Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void LoadIdentifiers() {
        File f = Tools.getFile(Tools.FileType.GET_AND_CREATE, "Identifiers.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        for (String key : config.getKeys(false)) {
            int v = config.getInt(key);
            if (v == -1) {
                continue;
            }
            Identifiers.put(key, config.getInt(key));
        }
    }

    public static int getIdentifier(Gem g) {
        if (Identifiers.containsKey(g.getName())) {
            return Identifiers.get(g.getName());
        }
        int max = 0;
        for (int value : Identifiers.values()) {
            if (value > max) {
                max = value;
            }
        }
        max++;
        Identifiers.put(g.getName(), max);
        SaveIdentifiers();
        return max;
    }

    private static void SaveIdentifiers() {
        File f = Tools.getFile(Tools.FileType.GET_AND_CREATE, "Identifiers.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        for (String key : config.getKeys(false)) {
            if (!Identifiers.containsKey(key)) {
                config.set(key, -1);
            }
        }
        for (Map.Entry<String, Integer> e : Identifiers.entrySet()) {
            config.set(e.getKey(), e.getValue());
        }
        try {
            config.save(f);
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
