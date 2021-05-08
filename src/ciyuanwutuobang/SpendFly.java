package ciyuanwutuobang;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 150149
 */
public class SpendFly extends JavaPlugin implements Listener {

    private Map<String, Boolean> isfly = new HashMap<>();
    private Map<String, BukkitTask> timer = new HashMap<>();
    private Map<String, BukkitTask> checker = new HashMap<>();
    private Map<String, Integer> mover = new HashMap<>();

    private String Feixingzhiling="cmi fly";
    //@param Feixingzhiling飞行指令修改处

    private int Jiezhi=60;
    //@param Jiezhi 饥饿值扣1的时间

    private int Jinbi=3;
    //@param Jinbi 每秒金币消耗量

    private boolean guagou =false;
    private static Economy economy = null;
    FileConfiguration config = getConfig();

    private void debug(String s){
        //Bukkit.getConsoleSender().sendMessage(s);
    }

    private boolean guaGouVault(){
      RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
       if(economyProvider == null) {
           guagou=false;
           return false;
       }
       else {
           economy=economyProvider.getProvider();
           guagou=true;
            return true;
        }
    }


    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        if(!guaGouVault()){
            Bukkit.getConsoleSender().sendMessage("[SpendFLy]挂钩vault失败，将禁用消费金币购买飞行功能");
        }

        config.addDefault("每秒克扣金币", 9);
        config.addDefault("每扣一点饱食度所需的秒数", 3);
        config.addDefault("基础插件开启飞行的指令(不带斜杠)","fly");
        config.options().copyDefaults(true);
        saveConfig();

        Jiezhi = config.getInt("每扣一点饱食度所需的秒数")*20;
        Jinbi = config.getInt("每秒克扣金币");
        Feixingzhiling = config.getString("基础插件开启飞行的指令(不带斜杠)");
        debug("饱食度:" + String.valueOf(Jiezhi));
        debug("金币:" + String.valueOf(Jinbi));
        debug("指令:" + Feixingzhiling);

        Bukkit.getConsoleSender().sendMessage("[SpendFLy]飞行花费已开启");
        Bukkit.getConsoleSender().sendMessage("[SpendFLy]作者：150149  QQ：1802796278");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("[SpendFLy]飞行花费已关闭");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){if ("spendfly".equalsIgnoreCase(cmd.getName())){

        if (!(sender instanceof Player)) {
            sender.sendMessage("[SpendFLy]该指令不能在控制台执行");
            return true;
        }
        debug("已过控制台验证");

        if (args.length>=2) {
            sender.sendMessage("§8[ §cSpendFLy §8]§7输入有误，请重新输入");
            return true;
        }
        debug("已过args.length过长验证，args.length="+args.length);

        if (args.length==0){
            if (sender.isOp()) {
                sender.sendMessage("§7§m---------------------§8[ §cSpendFLy §8]§7§m---------------------");
                sender.sendMessage("  ");
                sender.sendMessage("§7输入/spendfly food 消耗食物来飞行(OP,创造,有飞行权限不可用)");
                sender.sendMessage("§7输入/spendfly money 消耗金币来飞行(OP,创造,有飞行权限不可用)");
                sender.sendMessage("§7输入/spendfly stop 手动停止飞行(OP,创造,有飞行权限不可用)");
                sender.sendMessage("§7输入/spendfly reload 重载配置文件");
                sender.sendMessage("  ");
                sender.sendMessage("§7§m---------------------§8[ §cSpendFLy §8]§7§m---------------------");
            }
            else {
                sender.sendMessage("§7§m-------------§8[ §cSpendFLy §8]§7§m-------------");
                sender.sendMessage("  ");
                sender.sendMessage("§7输入/spendfly food 消耗食物来飞行");
                sender.sendMessage("§7输入/spendfly money 消耗金币来飞行");
                sender.sendMessage("§7输入/spendfly stop 手动停止飞行");
                sender.sendMessage("  ");
                sender.sendMessage("§7§m-------------§8[ §cSpendFLy §8]§7§m-------------");
            }
            return true;
        }
        debug("已过args过短验证");

        String aaa = args[0].toLowerCase();




        debug("创建玩家成功");
        Player p = (Player) sender;

        if ("reload".equals(aaa)) {
            if (p.isOp()) {
                config=getConfig();
                Jiezhi = config.getInt("每扣一点饱食度所需的秒数")*20;
                Jinbi = config.getInt("每秒克扣金币");
                Feixingzhiling = config.getString("基础插件开启飞行的指令(不带斜杠)");
                debug("饱食度:" + String.valueOf(Jiezhi));
                debug("金币:" + String.valueOf(Jinbi));
                debug("指令:" + Feixingzhiling);
                p.sendMessage("§8[ §cSpendFLy §8]§7重载完成");
                return true;
            }
            else {
                p.sendMessage("§8[ §cSpendFLy §8]§7你不是OP，你不能这么做");
                return true;
            }
        }

        if ("food".equals(aaa)) {

            if (p.getFoodLevel()<=0) {
                p.sendMessage("§8[ §cSpendFLy §8]§7饱食度过低，不能飞行");
                return true;
            }
            if (p.isFlying() || isfly.get(p.getPlayerListName())) {
                p.sendMessage("§8[ §cSpendFLy §8]§7你已经在飞行状态，不能开启");
                return true;
            }
            if (p.hasPermission("essentials.fly")) {
                p.sendMessage("§8[ §cSpendFLy §8]§7你已经有飞行权限，不能开启");
                return true;
            }
            if ("CREATIVE".equals(p.getGameMode().toString())) {
                p.sendMessage("§8[ §cSpendFLy §8]§7你是创造模式，不能开启");
                return true;
            }
            debug("已过各种验证");

            isfly.put(p.getPlayerListName(),true);
            p.sendMessage("§8[ §cSpendFLy §8]§7已开启飞行模式");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Feixingzhiling + " " + p.getPlayerListName());
            debug("发出指令：" + Feixingzhiling + " " + p.getPlayerListName());

            BukkitTask time=new BukkitRunnable() {
                @Override
                public void run() {
                    debug("----------------开始判断饱食度----------------");
                    if (p.getFoodLevel()<=0) {
                        if (isfly.get(p.getPlayerListName())) {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Feixingzhiling + " " + p.getPlayerListName());
                        }
                        isfly.put(p.getPlayerListName(),false);
                        this.cancel();
                        p.sendMessage("§8[ §cSpendFLy §8]§7你的饱食度过低，已停止飞行");
                    }
                    else{
                        p.setFoodLevel(p.getFoodLevel()-1);
                        debug("饱食度不为0，克扣，剩余："+p.getFoodLevel());
                    }
                    debug("开始判断飞行状态");
                    if (!isfly.get(p.getPlayerListName())) {
                        if (p.isFlying()) {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),Feixingzhiling + " " + p.getPlayerListName());
                        }
                        debug("不在飞行状态，取消定时器");
                        this.cancel();
                    }
                    debug("----------------一切都判断完成了----------------");

                }

            }.runTaskTimer(this, 20,Jiezhi);
            timer.put(p.getPlayerListName(),time);
            return true;
        }
        if ("money".equals(aaa)) {
            if (!guagou) {
                p.sendMessage("§8[ §cSpendFLy §8]§7未挂钩vault插件，无法使用金币飞行");
                return true;
            }
            debug("已通过挂钩测试");

            if (economy.getBalance(p)-Jinbi<0) {
                p.sendMessage("§8[ §cSpendFLy §8]§7金币不足，不能飞行");
                return true;
            }
            debug("已通过金币测试");

            if (p.isFlying() || isfly.get(p.getPlayerListName())) {
                p.sendMessage("§8[ §cSpendFLy §8]§7你已经在飞行状态，不能开启");
                return true;
            }
            debug("已通过飞行测试");

            if (p.hasPermission("essentials.fly")) {
                p.sendMessage("§8[ §cSpendFLy §8]§7你已经有飞行权限，不能开启");
                return true;
            }
            debug("已通过权限测试");

            if ("CREATIVE".equals(p.getGameMode().toString())) {
                p.sendMessage("§8[ §cSpendFLy §8]§7你是创造模式，不能开启");
                return true;
            }
            debug("已通过创造测试");

            debug("已过各种验证");

            isfly.put(p.getPlayerListName(),true);
            p.sendMessage("§8[ §cSpendFLy §8]§7已开启飞行模式");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),Feixingzhiling + " " + p.getPlayerListName());

            BukkitTask time=new BukkitRunnable() {
                @Override
                public void run() {
                    debug("----------------开始判断金币剩余----------------");
                    if (!economy.has(p,Jinbi)) {
                        if (isfly.get(p.getPlayerListName())) {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Feixingzhiling + " " + p.getPlayerListName());
                        }
                        isfly.put(p.getPlayerListName(),false);
                        this.cancel();
                        p.sendMessage("§8[ §cSpendFLy §8]§7你的金币不足，已停止飞行");
                    }
                    else{
                        economy.withdrawPlayer(p,Jinbi);
                        debug("金币充足，克扣，剩余："+economy.getBalance(p));
                    }
                    debug("开始判断飞行状态");
                    if (!isfly.get(p.getPlayerListName())) {
                        if (p.isFlying()) {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),Feixingzhiling + " " + p.getPlayerListName());
                        }
                        debug("不在飞行状态，取消定时器");
                        this.cancel();
                    }
                    debug("----------------一切都判断完成了----------------");

                }

            }.runTaskTimer(this, 20,20);
            timer.put(p.getPlayerListName(),time);
            return true;

        }
        if ("stop".equals(aaa)) {
            if (!isfly.get(p.getPlayerListName())){
                p.sendMessage("§8[ §cSpendFLy §8]§7未在飞行状态");
                if (p.isFlying()) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),Feixingzhiling + " " + p.getPlayerListName());
                }
                return true;
            }
            while(p.getLocation().add(0,-1,0).getBlock().isEmpty()){
                p.teleport(p.getLocation().add(0,-1,0));
            }
            if (isfly.get(p.getPlayerListName())) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),Feixingzhiling + " " + p.getPlayerListName());
            }
            isfly.put(p.getPlayerListName(),false);
            timer.get(p.getPlayerListName()).cancel();
            p.sendMessage("§8[ §cSpendFLy §8]§7已手动停止飞行");
            return true;
        }
        else {
            if (sender.isOp()) {
                sender.sendMessage("§7§m---------------------§8[ §cSpendFLy §8]§7§m---------------------");
                sender.sendMessage("  ");
                sender.sendMessage("§7输入/spendfly food 消耗食物来飞行(OP,创造,有飞行权限不可用)");
                sender.sendMessage("§7输入/spendfly money 消耗金币来飞行(OP,创造,有飞行权限不可用)");
                sender.sendMessage("§7输入/spendfly stop 手动停止飞行(OP,创造,有飞行权限不可用)");
                sender.sendMessage("§7输入/spendfly reload 重载配置文件");
                sender.sendMessage("  ");
                sender.sendMessage("§7§m---------------------§8[ §cSpendFLy §8]§7§m---------------------");
            }
            else {
                sender.sendMessage("§7§m-------------§8[ §cSpendFLy §8]§7§m-------------");
                sender.sendMessage("  ");
                sender.sendMessage("§7输入/spendfly food 消耗食物来飞行");
                sender.sendMessage("§7输入/spendfly money 消耗金币来飞行");
                sender.sendMessage("§7输入/spendfly stop 手动停止飞行");
                sender.sendMessage("  ");
                sender.sendMessage("§7§m-------------§8[ §cSpendFLy §8]§7§m-------------");
            }
            return true;
        }

    }
        return true;
    }


    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        if (isfly.get(event.getPlayer().getPlayerListName())) {
            event.getPlayer().sendMessage("§8[ §cSpendFLy §8]§7离开服务器，已停止飞行");
            debug("状态发生改变，停止计时器");
            if (event.getPlayer().isFlying()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Feixingzhiling + " " + event.getPlayer().getPlayerListName());
            }
            isfly.put(event.getPlayer().getPlayerListName(), false);
            timer.get(event.getPlayer().getPlayerListName()).cancel();
        }
        checker.get(event.getPlayer().getPlayerListName()).cancel();
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event){

        if (isfly.get(event.getPlayer().getPlayerListName())) {
            event.getPlayer().sendMessage("§8[ §cSpendFLy §8]§7世界传送，已停止飞行");
            debug("状态发生改变，停止计时器");
            isfly.put(event.getPlayer().getPlayerListName(), false);
            timer.get(event.getPlayer().getPlayerListName()).cancel();
        }
    }

    @EventHandler
    public void onChangeMode(PlayerGameModeChangeEvent event){

        if (isfly.get(event.getPlayer().getPlayerListName())) {
            event.getPlayer().sendMessage("§8[ §cSpendFLy §8]§7模式改变，已停止飞行");
            debug("状态发生改变，停止计时器");
            isfly.put(event.getPlayer().getPlayerListName(), false);
            timer.get(event.getPlayer().getPlayerListName()).cancel();
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){

        if (isfly.get(event.getPlayer().getPlayerListName())) {
            event.getPlayer().sendMessage("§8[ §cSpendFLy §8]§7玩家重生，已停止飞行");
            debug("状态发生改变，停止计时器");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Feixingzhiling + " " + event.getPlayer().getPlayerListName());
            isfly.put(event.getPlayer().getPlayerListName(), false);
            timer.get(event.getPlayer().getPlayerListName()).cancel();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        isfly.put(event.getPlayer().getPlayerListName(),false);
        BukkitTask time=new BukkitRunnable() {
            @Override
            public void run() {
                if (!isfly.get(event.getPlayer().getPlayerListName()) && event.getPlayer().isFlying()) {
                    if (!event.getPlayer().isOp() && !event.getPlayer().hasPermission("essentials.fly") && !"CREATIVE".equals(event.getPlayer().getGameMode().toString())) {
                        debug("检测到非法飞行");
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Feixingzhiling + " " + event.getPlayer().getPlayerListName());
                    }

                }
                if (isfly.get(event.getPlayer().getPlayerListName()) && mover.get(event.getPlayer().getPlayerListName())>0) {
                    mover.put(event.getPlayer().getPlayerListName(),mover.get(event.getPlayer().getPlayerListName())-1);
                }
                if (isfly.get(event.getPlayer().getPlayerListName()) && mover.get(event.getPlayer().getPlayerListName())<=0) {
                    event.getPlayer().sendMessage("§8[ §cSpendFLy §8]§7挂机时间过长，已停止飞行");
                    debug("状态发生改变，停止计时器");
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Feixingzhiling + " " + event.getPlayer().getPlayerListName());
                    isfly.put(event.getPlayer().getPlayerListName(), false);
                    timer.get(event.getPlayer().getPlayerListName()).cancel();
                }
            }
        }.runTaskTimer(this, 20,20);
        checker.put(event.getPlayer().getPlayerListName(),time);
    }

    @EventHandler
    public void onmove(PlayerMoveEvent event) {

        mover.put(event.getPlayer().getPlayerListName(),120);

    }
}
