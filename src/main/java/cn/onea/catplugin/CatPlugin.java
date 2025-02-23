package cn.onea.catplugin;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

public class CatPlugin extends JavaPlugin implements Listener {
    private boolean featureEnabled = true;
    private static final String ADMIN_PERMISSION = "cat.admin";
    private static final String CONFIG_KEY = "enabled";

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.featureEnabled = this.getConfig().getBoolean(CONFIG_KEY, true);
        this.getLogger().info("主人!喵喵牌插件已开启!");

        // 检查是否存在 home 节点
        if (!getConfig().contains("home")) {
            // 如果没有则设置默认值
            getConfig().set("home.x", 9);
            getConfig().set("home.y", 96);
            getConfig().set("home.z", 20);
            saveConfig();
            getLogger().info("已创建 home 配置项");
        } else {
            getLogger().info("home 配置项已存在，跳过创建");
        }

        Bukkit.getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        this.getConfig().set(CONFIG_KEY, this.featureEnabled);
        this.saveConfig();
        this.getLogger().info("主人!喵喵牌插件已卸载");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String @NotNull [] args) {
        if (cmd.getName().equalsIgnoreCase("catkill")) {
            handleCatkillCommand(sender);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("hub")) {
            handleHubCommand(sender);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("catconfig")) {
            handleCatConfigCommand(sender, args);
            return true;
        }
        return false;
    }

    private void handleCatkillCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用这个命令喵！");
            return;
        }
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用这个命令喵！");
            return;
        }
        // 使用配置中的 home 坐标
        int x = getConfig().getInt("home.x");
        int y = getConfig().getInt("home.y");
        int z = getConfig().getInt("home.z");
        String command = "execute in minecraft:the_void run minecraft:tp " + player.getName() + " " + x + " " + y + " " + z;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        // 设置玩家为冒险模式
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage("§6传送成功！你已进入主城，当前游戏模式为冒险。");
    }

    /**
     * 处理 /catconfig 命令
     * 支持以下两种格式：
     * /catconfig set home <x> <y> <z>
     * /catconfig set catkill <true|false>
     */
    private void handleCatConfigCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("§c你没有权限使用该命令！");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("§6用法: /catconfig set <home|catkill> ...");
            return;
        }
        if (!args[0].equalsIgnoreCase("set")) {
            sender.sendMessage("§6用法: /catconfig set <home|catkill> ...");
            return;
        }

        // 处理 home 坐标修改
        if (args[1].equalsIgnoreCase("home")) {
            if (args.length != 5) {
                sender.sendMessage("§6用法: /catconfig set home <x> <y> <z>");
                return;
            }
            try {
                int x = Integer.parseInt(args[2]);
                int y = Integer.parseInt(args[3]);
                int z = Integer.parseInt(args[4]);
                // 更新配置
                getConfig().set("home.x", x);
                getConfig().set("home.y", y);
                getConfig().set("home.z", z);
                saveConfig();
                sender.sendMessage("§a已将 home 坐标修改为: " + x + ", " + y + ", " + z);
                getLogger().info("Home 坐标更新为: " + x + ", " + y + ", " + z);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c坐标必须为整数！");
            }
            return;
        }

        // 处理 catkill 功能开关修改
        if (args[1].equalsIgnoreCase("catkill")) {
            if (args.length != 3) {
                sender.sendMessage("§6用法: /catconfig set catkill <true|false>");
                return;
            }
            String value = args[2].toLowerCase();
            if (value.equals("true") || value.equals("false")) {
                this.featureEnabled = Boolean.parseBoolean(value);
                // 同步更新配置文件
                getConfig().set(CONFIG_KEY, this.featureEnabled);
                saveConfig();
                sender.sendMessage("§a已将 catkill 功能设置为: " + this.featureEnabled);
                getLogger().info("catkill 功能设置为: " + this.featureEnabled);
            } else {
                sender.sendMessage("§c参数错误，必须为 true 或 false");
            }
            return;
        }

        sender.sendMessage("§6用法: /catconfig set <home|catkill> ...");
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
