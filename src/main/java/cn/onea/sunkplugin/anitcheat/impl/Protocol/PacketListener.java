package cn.onea.sunkplugin.anitcheat.impl.Protocol;
// 数据包监听
import cn.onea.sunkplugin.SunkPlugins;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

// 实体操作
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class PacketListener {
    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(
                        SunkPlugins.getPlugin(SunkPlugins.class),
                        ListenerPriority.HIGH,
                        PacketType.Play.Client.ENTITY_ACTION
                ) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        // 检测异常跳跃数据包
                        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
                            int action = (int) event.getPacket().getModifier().read(0);
                            if (action == 3) { // START_JUMPING
                                Player player = event.getPlayer();
                                if (player.isOnGround()) {
                                    // 地面状态跳跃合法检测
                                    analyzeJumpConsistency(player);
                                }
                            }
                        }
                    }
                });
    }

    private void analyzeJumpConsistency(Player player) {

    }
}
