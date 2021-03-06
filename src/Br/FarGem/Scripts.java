/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package Br.FarGem;

import Br.API.Scripts.ScriptListener;
import Br.API.Scripts.ScriptListenerManager;
import Br.API.Scripts.ScriptLoader;
import Br.API.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 */
public class Scripts {
    
    public static List<Runnable> LoadFromScripts = new ArrayList<>();

    public static void LoadScripts(Main p) {
        File folder = new File(p.getDataFolder(), File.separator + "Scripts" + File.separator);
        if (!folder.exists()) {
            try {
                Utils.OutputFile(p, "example.js", folder);
            } catch (IOException ex) {
                Logger.getLogger(Scripts.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(!LoadFromScripts.isEmpty()){
            for (Runnable r : LoadFromScripts) {
                r.run();
            }
            LoadFromScripts.clear();
        }
        for (File f : folder.listFiles()) {
            ScriptLoader.eval(p, (t) -> {
                try {
                    t.eval(new InputStreamReader(new FileInputStream(f), "UTF-8"));
                    boolean enable = (boolean) t.invokeFunction("isEnable", new Object[]{});
                    if (!enable) {
                        return;
                    }
                    Gem gem = (Gem) t.invokeFunction("getGem", new Object[]{});
                    ScriptListener sl[] = (ScriptListener[]) t.invokeFunction("getListener", new Object[]{});
                    BukkitTask task = null;
                    if (gem instanceof GemRunnable) {
                        GemRunnable gr = (GemRunnable) gem;
                        task = Bukkit.getScheduler().runTaskTimer(p, gr::run, gr.delay(), gr.interval());
                    }
                    BukkitTask var = task;
                    Data.LoadGemData(gem);
                    for (ScriptListener l : sl) {
                        ScriptListenerManager.RegisterListener(p, l);
                    }
                    LoadFromScripts.add(() -> {
                        for (ScriptListener l : sl) {
                            HandlerList.unregisterAll(l);
                        }
                        if(var != null){
                            var.cancel();
                        }
                        Data.GemIDMap.remove(gem.getIdentifier());
                        
                    });
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Scripts.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ScriptException ex) {
                    Logger.getLogger(Scripts.class.getName()).log(Level.SEVERE, f.getName() + " 脚本异常", ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(Scripts.class.getName()).log(Level.SEVERE, f.getName() + " 找不到方法", ex);
                } catch (Throwable tt) {
                    Logger.getLogger(Scripts.class.getName()).log(Level.SEVERE, f.getName() + " 脚本异常", tt);
                }
            });
        }
    }

    private Scripts() {
    }
}
