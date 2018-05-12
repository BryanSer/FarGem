/* global Java */

var Enable = true;

var Gem = Java.type('Br.FarGem.Gem');
var OptionalGemDisplay = Java.type('Br.FarGem.OptionalGemDisplay');
var Config = Java.type('Br.FarGem.Gem.Config');
var Material = Java.type('org.bukkit.Material');
var Player = Java.type('org.bukkit.entity.Player');
var System = Java.type('java.lang.System');

var GemListener = Java.type('Br.FarGem.GemListener');

var ScriptListener = Java.extend(Java.type('Br.API.Scripts.ScriptListener'));
var FireGemType = Java.extend(Gem, OptionalGemDisplay);

var GemLore = new Config('GemLore', function (t) {
    var lore = new java.util.ArrayList();
    lore.add('§c火焰石 §b等级:' + t);
    lore.add('§b火焰附加+' + t * 2);
    lore.add('仅能镶嵌到斧头或剑上');
    return lore;
});
var DisplayLore = new Config('DisplayLore', function (lv) {
    return '§c火焰石 ' + lv + '级   -   火焰附加时间+' + (lv * 2);
});
var TimeTick = new Config('TimeTick', function (t) {
    return t * 10;
});

var FireGem = new FireGemType('FireGem', '§c火焰石', 5){
    getDisplayLore: function (lv) {
        return DisplayLore.getValue(lv);
    },
    getConfigs: function () {
        return Java.to([GemLore, DisplayLore, TimeTick], 'Br.FarGem.Gem.Config[]');
    },
    canInstall: function (is) {
        var s = is.getType().toString();
        return s.contains('_AXE') || s.contains('_SWORD');
    },
    getLore: function (lv) {
        return GemLore.getValue(lv);
    }
};
var Listener = new ScriptListener(){
    onEvent: function (evt) {
        if (Player.class.isInstance(evt.getDamager())) {
            var p = Player.class.cast(evt.getDamager());
            var is = p.getItemInHand();
            var level = FireGem.getEquipLevel(is);
            evt.getEntity().setFireTicks(TimeTick.getValue(level));
        }
    },
    getEventName: function () {
        return 'org.bukkit.event.entity.EntityDamageByEntityEvent';
    }
};
function getGem() {
    return FireGem;
}
function getListener() {
    return Java.to([Listener], 'Br.API.Scripts.ScriptListener[]');
}

function isEnable() {
    return Enable;
}