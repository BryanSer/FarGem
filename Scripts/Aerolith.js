/* global Java */
//这个文件写不下去了
//请不要使用这个文件
var Enable = true;

var Gem = Java.type('Br.FarGem.Gem');
var OptionalGemDisplay = Java.type('Br.FarGem.OptionalGemDisplay');
var Config = Java.type('Br.FarGem.Gem.Config');
var Material = Java.type('org.bukkit.Material');
var Player = Java.type('org.bukkit.entity.Player');
var System = Java.type('java.lang.System');
var DecimalFormat = Java.type('java.text.DecimalFormat');
var FixedMetadataValue = Java.type('org.bukkit.metadata.FixedMetadataValue');
var MetadataValue = Java.type('org.bukkit.metadata.MetadataValue');
var Location = Java.type('org.bukkit.Location');
var Data = Java.type('Br.Artifice.Data');

var GemListener = Java.type('Br.FarGem.GemListener');
var ScriptListener = Java.extend(Java.type('Br.API.Scripts.ScriptListener'));
var AerolithGemType = Java.extend(Gem, OptionalGemDisplay);

var GemLore = new Config('GemLore', function (t) {
    var lore = new java.util.ArrayList();
    lore.add('§5陨铁石 §b等级-' + t);
    lore.add('§b陨石射程' + (100 + 10 * t));
    lore.add('§b陨石伤害' + (20 + 5 * t));
    lore.add('仅能镶嵌到烈焰棒上');
    return lore;
});
var InstallItem = new Config('InstallItem', 369);//烈焰棒ID
var DisplayLore = new Config('DisplayLore', function (lv) {
    var s = '陨铁石';
    switch (lv) {
        case 1:
            s = '一级' + s;
            break;
        case 2:
            s = '二级' + s;
            break;
        case 3:
            s = '三级' + s;
            break;
        case 4:
            s = '四级' + s;
            break;
        case 5:
            s = '五级' + s;
            break;
    }
    return '§c§l' + s;
});
var Damage = new Config("Damage",function (lv){return 20.0 + 5.0 * lv;});
var MaxLength = new Config("MaxLength", function (lv){return 100 + 10.0 * lv;});
var ColdDown = new Config("ColdDown", function (lv){return 22.0 - 5.0 * lv;});

var AerolithGem = new AerolithGemType("AerolithGem","§c陨铁石",5){
 	getDisplayLore: function (lv) {
        return DisplayLore.getValue(lv);
    },
    getConfigs: function (lv) {
        return Java.to([GemLore, InstallItem, DisplayLore, Damage, ColdDown, MaxLength],
                'Br.FarGem.Gem.Config[]');
    },
    canInstall: function (is) {
        return is.getTypeId() === InstallItem.getValue();
    },
    getLore: function (lv) {
        return GemLore.getValue(lv);
    },
    getDisplayName: function (lv) {
        return '§b'+lv + '级 陨铁石';
    }
};
function Cast(p,lv){
	var fb = p.launchProjectile(Fireball.class, p.getLocation().getDirection().multiply(0.8));
    fb.setMetadata("Artifice_Aerolith", new FixedMetadataValue(Data.Plugin, p.getName() + "|" + Damage.getValue(lv)));
    var oriloc = fb.getLocation();
    Bukkit.getScheduler().runTaskTimer(Data.Plugin,function (){
    	var loc = fb.getLocation();
    	var d = MaxLength.getValue(lv) * MaxLength.getValue(lv) - loc.distanceSquared(oriloc);
    	if (d < 10) {
            fb.setVelocity(fb.getVelocity().setY(-0.6));
        }else {
            fb.setVelocity(fb.getVelocity().add(new Vector(0, -0.02, 0)));
        }
    }, 1, 1);
}
var CD = new java.util.HashMap();
var df = new DecimalFormat('#.#');
var CastListener = new ScriptListener(){
    onEvent: function (evt) {
    	if (!evt.hasItem()) {
            return;
        }
        if (!FlashGem.canInstall(evt.getItem())) {
            return;
        }
        if (GemListener.isInstalling(evt.getPlayer())) {
            return;
        }
        var level = FlashGem.getEquipLevel(evt.getItem());
        if (level < 0) {
            return;
        }
        evt.setCancelled(true);
        var p = evt.getPlayer();
        var cd = ColdDown.getValue(level);
        if (CD.containsKey(p.getName()) && CD.get(p.getName()) + cd * 1000 > System.currentTimeMillis()) {
            var le = CD.get(p.getName()) + cd * 1000 - System.currentTimeMillis();
            p.sendMessage('§c冷却未完成 剩余冷却时间:  ' + df.format(le / 1000));
            return;
        }
        Cast(p,level);
    }
};