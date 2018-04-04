/* 
 * 开发者:Bryan_lzh
 * QQ:390807154
 */
/* global Java */

var Enable = false;


var Gem = Java.type('Br.FarGem.Gem');
var GemDisplay = Java.type('Br.FarGem.GemDisplay');
var Config = Java.type('Br.FarGem.Gem.Config');
var Material = Java.type('org.bukkit.Material');
var Player = Java.type('org.bukkit.entity.Player');
var EntityType = Java.type('org.bukkit.entity.EntityType');

var ScriptListener = Java.extend(Java.type('Br.API.Scripts.ScriptListener'));
var AttackGemType = Java.extend(Gem, GemDisplay);

var DisplayLoreConfig = new Config("DisplayLore", function (lv) {
    return "§c" + lv + "级 攻击宝石  攻击力+" + lv * 3;
}, true);
var GemLoreConfig = new Config("GemLoreConfig", function (lv) {
    var list = new java.util.ArrayList();
    list.add("§c攻击宝石  等级: " + lv);
    list.add("§b增加攻击力: " + lv * 3);
    list.add("§6仅能安装到剑或斧头上");
    list.add("§a右键宝石开始安装");
    return list;
}, true);
var AttackConfig = new Config("Attack", function (lv) {
    return lv * 3;
}, true);

var AttackGem = new AttackGemType('AttackGem', "§c攻击宝石", 10){
    getDisplayLore: function (lv) {
        return DisplayLoreConfig.getValue(lv);
    },
    getConfigs: function () {
        return Java.to([DisplayLoreConfig, GemLoreConfig, AttackConfig], "Br.FarGem.Gem.Config[]");
    },
    canInstall: function (is) {
        var s = is.getType().toString();
        return s.contains("_AXE") || s.contains("_SWORD");
    },
    getType: function (lv) {
        return Material.EMERALD;
    },
    getDurability: function (lv) {
        return 0;
    },
    getDisplayName: function (lv) {
        return "§c" + lv + "级 攻击宝石";
    },
    getLore: function (lv) {
        return GemLoreConfig.getValue(lv);
    }
};
var Listener = new ScriptListener(){
    onEvent: function (evt) {
        if (evt.getDamager().getType() !== EntityType.PLAYER) {
            return;
        }
        var p = evt.getDamager();
        var is = p.getItemInHand();
        var lv = Java.super(AttackGem).getEquipLevel(is);
        if (lv > 0) {
            evt.setDamage(evt.getDamage() + AttackConfig.getValue(lv));
        }
    },
    getEventClass: function () {
        return Java.tpye('org.bukkit.event.entity.EntityDamageByEntityEvent');
    }
}
function getGem() {
    return AttackGem;
}


function getListener() {
    return Java.to([Listener], "Br.API.Scripts.ScriptListener[]")
}

function isEnable(){
    return Enable;
}