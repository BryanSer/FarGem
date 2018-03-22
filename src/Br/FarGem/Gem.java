/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem;

import Br.API.Lores;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public abstract class Gem implements Listener {

    protected String Name;
    protected String DisplayName;
    private int ID;
    protected int MaxLevel;

    public Gem(String name, String displayName, int maxlv) {
        this.Name = name;
        this.DisplayName = displayName;
        this.MaxLevel = maxlv;
        this.ID = Data.getIdentifier(this);
        Data.GemIDMap.put(this.ID, this);
    }

    public abstract String getDisplayLore(int lv);

    public abstract Config<Object>[] getConfigs();

    public abstract boolean canInstall(ItemStack is);

    public int getMaxLevel() {
        return MaxLevel;
    }

    public final int getIdentifier() {
        return this.ID;
    }

    public String getName() {
        return Name;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public ItemStack getGem(int level) {
        if (level < 1 || level > this.getMaxLevel()) {
            return null;
        }
        ItemStack is = null;
        if (this instanceof GemDisplay) {
            GemDisplay gp = (GemDisplay) this;
            is = new ItemStack(gp.getType(level), 1, gp.getDurability(level));
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(gp.getDisplayName(level));
            im.setLore(gp.getLore(level));
            is.setItemMeta(im);
        } else {
            is = new ItemStack(Material.EMERALD);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(this.getDisplayName());
            is.setItemMeta(im);
        }
        ItemMeta im = is.getItemMeta();
        List<String> lore = im.hasLore() ? new ArrayList<>(im.getLore()) : new ArrayList<>();
        lore.add(this.getGemDisplayLore(level));
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }

    public final ItemStack Install(ItemStack is, int level) {
        if (level < 1 || level > this.getMaxLevel() || is == null) {
            return null;
        }
        is = is.clone();
        int max = Tools.MaxInstallFuncion.apply(is);
        if (max == 0) {
            return null;
        } else if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
            int installed = 0;
            for (String s : is.getItemMeta().getLore()) {
                if (s.contains(Tools.encodeColorCode(Tools.IdentifierPrefix))) {
                    installed++;
                }
            }
            if (installed >= max) {
                return null;
            }
        }
        int SpLine = -1;//分隔线所在的行数
        boolean replace = false;
        if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
            int i = 0;
            for (String s : is.getItemMeta().getLore()) {
                if (s.equals(Data.InstallPrefix_Value)) {
                    SpLine = i;
                }
                if (s.startsWith(Tools.encodeColorCode(Tools.IdentifierPrefix + String.valueOf(this.getIdentifier())))) {
                    SpLine = i;
                    replace = true;
                    break;
                }
                i++;
            }
        }
        if (SpLine == -1) {
            is = Lores.addLores(is, new String[]{
                Data.InstallPrefix_Value,
                this.getEquipDisplayLore(level)
            });
            return is;
        } else {
            if (replace) {
                is = Lores.setLore(is, SpLine,
                        this.getEquipDisplayLore(level));
                return is;
            }
            List<String> newlore = new ArrayList<>();
            int index = 0;
            for (String s : is.getItemMeta().getLore()) {
                newlore.add(s);
                if (index == SpLine) {
                    newlore.add(Tools.encodeColorCode(
                            this.getEquipDisplayLore(level)));
                }
                index++;
            }
            ItemMeta im = is.getItemMeta();
            im.setLore(newlore);
            is.setItemMeta(im);
        }
        return is;
    }

    public final String getGemDisplayLore(int lv) {
        return Tools.encodeColorCode(Tools.IdentifierPrefix + this.getIdentifier() + "|Gem|" + lv + Tools.IdentifierSuffix) + getDisplayLore(lv);
    }

    public final String getEquipDisplayLore(int lv) {
        return Tools.encodeColorCode(Tools.IdentifierPrefix + this.getIdentifier() + "|Equip|" + lv + Tools.IdentifierSuffix) + getDisplayLore(lv);
    }

    public final int getEquipLevel(ItemStack is) {
        if (is == null) {
            return 0;
        }
        if (!is.hasItemMeta() || !is.getItemMeta().hasLore()) {
            return 0;
        }
        List<String> lore = is.getItemMeta().getLore();
        for (String s : lore) {
            s = Tools.getIdentifier(s);
            if (s != null) {
                if (s.contains(String.valueOf(this.getIdentifier()))) {
                    String v[] = s.split("\\|");
                    if (v.length != 3) {
                        return 0;
                    }
                    if (v[1].equals("Equip")) {
                        return Integer.parseInt(v[2]);
                    }
                    return 0;
                }
            }
        }
        return 0;
    }

    /**
     * @see Tools#getGemInfo
     * @param is
     * @return
     * @deprecated
     */
    @Deprecated
    public final int getGemLevel(ItemStack is) {
        if (!is.hasItemMeta() || !is.getItemMeta().hasLore()) {
            return 0;
        }
        List<String> lore = is.getItemMeta().getLore();
        String code = lore.get(lore.size() - 1);
        code = Tools.getIdentifier(code);
        if (code != null) {
            String s[] = code.split("\\|");
            if (s.length != 3) {
                return 0;
            }
            if (!s[0].equals(String.valueOf(this.getIdentifier()))) {
                return 0;
            }
            if (!s[1].equals("Gem")) {
                return 0;
            }
            return Integer.parseInt(s[2]);
        }
        return 0;
    }

    /**
     * 形式注册方法
     */
    public final void Register() {
        Bukkit.getPluginManager().registerEvents(this, Data.Plugin);
        Data.LoadGemData(this);
        if(this instanceof GemRunnable){
            GemRunnable gr = (GemRunnable) this;
            Bukkit.getScheduler().runTaskTimer(Data.Plugin, gr::run, gr.delay(), gr.interval());
        }
    }

    public static class Config<V extends Object> {

        private Function<Integer, V> Values;
        private final boolean needLevel;
        private final String Name;

        public Config(String Name, Function<Integer, V> Values, boolean needLevel) {
            this.Values = Values;
            this.needLevel = needLevel;
            this.Name = Name;
        }

        public void setFunction(Function<Integer, V> f) {
            this.Values = f;
        }

        public V getValue(int lv) {
            return Values.apply(lv);
        }

        public V getValue() {
            if (needLevel) {
                throw new UnsupportedOperationException();
            }
            return getValue(0);
        }

        public boolean isNeedLevel() {
            return needLevel;
        }

        public String getName() {
            return Name;
        }
    }
}
