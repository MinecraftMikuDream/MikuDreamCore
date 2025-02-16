package cn.onea.catplugin;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CatPlugin extends JavaPlugin implements Listener {
    private boolean featureEnabled = true;
    private static final String ADMIN_PERMISSION = "cat.admin";
    private static final String CONFIG_KEY = "enabled";

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.featureEnabled = this.getConfig().getBoolean(CONFIG_KEY, true);
        this.getLogger().info("主人!喵喵牌插件已开启! 自杀功能状态: " + this.featureEnabled);
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.getConfig().set(CONFIG_KEY, this.featureEnabled);
        this.saveConfig();
        this.getLogger().info("主人!喵喵牌插件已卸载");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("catkill")) {
            // 处理自杀命令（保留原功能）
            handleCatkillCommand(sender, args);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("hub")) {
            handleHubCommand(sender);
            return true;
        }
        return false;
    }

    private void handleCatkillCommand(CommandSender sender, String[] args) {
        // 如果存在参数，则视为管理员指令，用于开启/关闭自杀功能
        if (args.length == 1) {
            if (!sender.hasPermission(ADMIN_PERMISSION)) {
                sender.sendMessage("§c你没有权限使用此命令！");
                return;
            }
            switch (args[0].toLowerCase()) {
                case "on":
                    this.featureEnabled = true;
                    saveConfigChange();
                    sender.sendMessage("§a已开启自杀功能");
                    break;
                case "off":
                    this.featureEnabled = false;
                    saveConfigChange();
                    sender.sendMessage("§c已关闭自杀功能");
                    break;
                default:
                    sender.sendMessage("§6用法: /catkill [on|off]");
                    break;
            }
            return;
        }
        // 无参数时，作为玩家执行自杀功能
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以使用这个命令喵！");
            return;
        }
        Player player = (Player) sender;
        if (!this.featureEnabled) {
            player.sendMessage("§c当前自杀功能已被管理员关闭喵！");
            return;
        }
        // 玩家自杀
        player.setHealth(0.0);
        player.sendMessage("§6你已成功自杀喵");
        Bukkit.broadcastMessage("§e玩家 " + player.getName() + " 选择了自我了断喵，可惜捏");
    }

    private void handleHubCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以使用这个命令喵！");
            return;
        }
        Player player = (Player) sender;
        World voidWorld = Bukkit.getWorld("the_void");
        if (voidWorld == null) {
            player.sendMessage("[error] §c无法找到名为 the_void 的维度！");
            return;
        }
        Location target = new Location(voidWorld, 0, 90, 0);
        player.teleport(target);
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage("§6传送成功！你已进入主城，当前游戏模式为冒险。");
    }

    private void saveConfigChange() {
        this.getConfig().set(CONFIG_KEY, this.featureEnabled);
        this.saveConfig();
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (event.getFrom().getName().equalsIgnoreCase("the_void")) {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.ADVENTURE) {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage("§e你已离开主城，游戏模式已切换为生存");
            }
        }
    }
}
