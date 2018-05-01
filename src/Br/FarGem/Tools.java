/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem;

import Br.API.Utils;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public class Tools {

    /**
     * 作为识别码的开头
     */
    public static final String IdentifierPrefix = "ridpr";

    /**
     * 作为识别码的结束
     */
    public static final String IdentifierSuffix = "ridsr";

    /**
     * 注 通过覆盖该lambda表达式可以修改每个物品最大镶嵌宝石数量
     */
    public static Function<ItemStack, Integer> MaxInstallFuncion = (is) -> Integer.MAX_VALUE;

    /**
     * 默认 FileType = GET_AND_CREATE
     *
     * @see FileType
     * @param s
     * @return
     */
    public static File getFile(String... s) {
        return Tools.getFile(FileType.GET_AND_CREATE, s);
    }

    public enum FileType {
        GET,
        GET_AND_CREATE,
        GET_AND_SAVE_DEFAULT;
    }

    /**
     * 返回文件
     *
     * @param create
     * @param s
     * @return
     */
    public static File getFile(FileType create, String... s) {
        StringBuilder url = new StringBuilder();
        if (s.length == 0) {
            url.append(s[0]);
        } else {
            for (int i = 0; i < s.length; i++) {
                url.append(File.separator).append(s[i]);
            }
        }
        File f = new File(Data.Plugin.getDataFolder(), url.toString());
        if (create != FileType.GET) {
            if (f.isDirectory() && !f.exists()) {
                f.mkdirs();
            } else {
                File pf = f.getParentFile();
                if (!pf.exists()) {
                    pf.mkdirs();
                }
                if (!f.exists()) {
                    try {
                        if (create == FileType.GET_AND_CREATE) {
                            f.createNewFile();
                        } else {
                            String[] fs = url.toString().split(File.separator);
                            Utils.OutputFile(Data.Plugin, fs[fs.length - 1], f.getParentFile());
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return f;
    }

    /**
     * 加上颜色代码
     *
     * @param s
     * @return
     */
    public static String encodeColorCode(String s) {
        char[] c = s.toCharArray();
        StringBuilder v = new StringBuilder();
        for (char d : c) {
            if (d == '§') {
                continue;
            }
            v.append('§').append(d);
        }
        return v.toString();
    }

    /**
     * 从混杂的信息中获取识别码<p>
     * 返回null表示找不到识别码
     *
     * @param s
     * @return
     */
    public static String getIdentifier(String s) {
        if (s == null) {
            return null;
        }
        s = Tools.decodeColorCode(s);
        if (s.contains(Tools.IdentifierPrefix) && s.contains(Tools.IdentifierSuffix)) {
            try {
                return s.split(Tools.IdentifierPrefix, 2)[1].split(Tools.IdentifierSuffix, 2)[0];
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
        return null;
    }

    /**
     * 去除颜色代码
     *
     * @param s
     * @return
     */
    public static String decodeColorCode(String s) {
        return s.replaceAll("§", "");
    }

    /**
     * 获取宝石物品的信息
     *
     * @param is
     * @return null时为该物品不是宝石
     */
    public static GemInfo getGemInfo(ItemStack is) {
        if (is == null || !is.hasItemMeta() || !is.getItemMeta().hasLore()) {
            return null;
        }
        List<String> lore = is.getItemMeta().getLore();
        String code = lore.get(lore.size() - 1);
        code = Tools.getIdentifier(code);
        if (code == null) {
            return null;
        }
        if (!code.matches(Gem.GemRegEX)) {
            return null;
        }
        try {
            String s[] = code.split("\\|");
            int id = Integer.parseInt(s[0]);
            Gem g = Data.GemIDMap.get(id);
            if (g == null) {
                return null;
            }
            int lv = Integer.parseInt(s[2]);
            return new GemInfo(g, lv);
        } catch (NumberFormatException e) {

        }
        return null;
    }

    /**
     * 宝石物品信息
     */
    public static class GemInfo {

        private Gem gem;
        private int level;

        private GemInfo(Gem g, int lv) {
            this.gem = g;
            this.level = lv;
        }

        /**
         * 返回该物品对应的宝石对象
         *
         * @return
         */
        public Gem getGem() {
            return gem;
        }

        /**
         * 返回等级
         *
         * @return
         */
        public int getLevel() {
            return level;
        }

    }

    public static Set<Gem> getInstalledGem(ItemStack is) {
        if (is == null || !is.hasItemMeta() || !is.getItemMeta().hasLore()) {
            return null;
        }
        Set<Gem> gems = new HashSet<>();
        for (String s : is.getItemMeta().getLore()) {
            s = Tools.getIdentifier(s);
            if (s == null) {
                continue;
            }
            String v[] = s.split("\\|");
            if (v[1].equals("Gem")) {
                return null;
            }
            int id = Integer.parseInt(v[0]);
            gems.add(Data.GemIDMap.get(id));
        }
        return gems;
    }

    public static ItemStack updateItem(ItemStack is) {
        if (is == null || !is.hasItemMeta() || !is.getItemMeta().hasLore()) {
            return null;
        }
        is = is.clone();
        boolean edit = false;
        ItemMeta im = is.getItemMeta();
        List<String> lore = im.getLore();
        for (int i = lore.size() - 1; i >= 0; i--) {
            String s = lore.get(i);
            String vs = getIdentifier(s);
            if (vs == null) {
                continue;
            }
            String v[] = vs.split("\\|");
            Gem g = Data.GemIDMap.get(Integer.parseInt(v[0]));
            if (g == null) {
                continue;
            }
            int lv = Integer.parseInt(v[2]);
            if (v[1].equals("Gem")) {
                if (!s.equals(g.getGemDisplayLore(lv))) {
                    lore.set(i, g.getGemDisplayLore(lv));
                    edit = true;
                }
                break;
            } else if (v[1].equals("Equip")) {
                if (!s.equals(g.getEquipDisplayLore(lv))) {
                    lore.set(i, g.getEquipDisplayLore(lv));
                    edit = true;
                }
            }
        }
        if (edit) {
            im.setLore(lore);
            is.setItemMeta(im);
            return is;
        }
        return null;
    }

    private Tools() {
    }
}
