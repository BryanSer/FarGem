var Enable = true;

var Gem = Java.type('Br.FarGem.Gem');
var OptionalGemDisplay = Java.type('Br.FarGem.OptionalGemDisplay');
var Config = Java.type('Br.FarGem.Gem.Config');
var Material = Java.type('org.bukkit.Material');
var Player = Java.type('org.bukkit.entity.Player');
var System = Java.type('java.lang.System');

var GemListener = Java.type('Br.FarGem.GemListener');

var ScriptListener = Java.extend(Java.type('Br.API.Scripts.ScriptListener'));
var FlashGemType = Java.extend(Gem, OptionalGemDisplay);

var GemLore = new Config('GemLore', function (lv) {
    var lore = new java.util.ArrayList();
    lore.add('§5闪现石 §b等级:' + t);
    lore.add('§b闪现距离' + t * 3);
    lore.add('仅能镶嵌到粘液球上');
    return list;
});
var InstallItem = new Config('InstallItem', 341);//粘液球ID
var DisplayLore = new Config('DisplayLore', function (lv) {
    var s = '闪现石';
    switch (level) {
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
    return '§a§l' + s + '   -   闪现距离:' + level * 3;
});
var Distance = new Config('Distance', function (lv) {
    return lv * 3;
});//闪现距离
var ColdDown = new Config('ColdDown', function (lv) {
    return 9 - lv;
});//冷却时间
var Cost = new Config('Cost', function (lv) {
    return 2 * lv;
});//闪现消耗的饱食度


var FlashGem = new FlashGemType('FlashGem', '§b闪现石', 5){
    getDisplayLore: function (lv) {
        return DisplayLore.getValue(lv);
    },
    getConfigs: function (lv) {
        return Java.to([GemLore, InstallItem, DisplayLore, Distance, ColdDown, Cost],
                'Br.FarGem.Gem.Config[]');
    },
    canInstall: function (is) {
        return is.getTypeId() === InstallItem.getValue();
    },
    getLore: function (lv) {
        return GemLore.getValue(lv);
    },
    getDisplayName: function (lv) {
        return '§b' + '级 闪现石';
    }
};
var CD = new java.util.HashMap();
var DecimalFormat = Java.type('java.text.DecimalFormat');
var df = new DecimalFormat('#.#');
var Listener = new ScriptListener(){
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
        evt.setCanceled(true);
        var p = evt.getPlayer();
        var cd = ColdDown.getValue(level);
        if (CD.containsKey(p.getName()) && CD.get(p.getName()) + cd * 1000 > System.currentTimeMillis()) {
            var le = CD.get(p.getName()) + cd * 1000 - System.currentTimeMillis();
            p.sendMessage('§c冷却未完成 剩余冷却时间:  ' + df.format(le / 1000));
            return;
        }
        if (p.getFoodLevel() < Cost.getValue(level)) {
            p.sendMessage('§c你的饥饿值不足 不能够完成闪现');
            return;
        }
        p.setFoodLevel(p.getFoodLevel() - Cost.getValue(level));
        var ori = p.getLocation().getDirection();
        ori.setY(0);
        ori.normalize();
        var fi = p.getLocation();
        var d = Distance.getValue(level);
        for (var i = 0; i < d; i++) {
            var v = ori.clone().multiply(i);
            var loc = p.getLocation().add(v);
            if (loc.getBlock().getType() != null && loc.getBlock().getType() != Material.AIR) {
                break;
            }
            fi = loc;
        }
        p.teleport(fi);
        p.sendMessage("§6已完成闪现");
        CD.put(p.getName(), System.currentTimeMillis());
    },
    getEventName: function () {
        return 'rg.bukkit.event.player.PlayerInteractEvent';
    }
};
function getGem() {
    return FlashGem;
}
function getListener() {
    return Java.to([Listener], "Br.API.Scripts.ScriptListener[]");
}

function isEnable() {
    return Enable;
}