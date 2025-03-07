package cn.onea.sunkplugin.anitcheat.impl.Protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

import static org.bukkit.Bukkit.getLogger;

public class FakeEntityHandler {
    // 向客户端发送虚假实体数据包
    public void sendDecoyEntities(Player player) {
        PacketContainer fakeEntity = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        fakeEntity.getModifier().writeDefaults();
        fakeEntity.getUUIDs().write(0, UUID.randomUUID());
        fakeEntity.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, fakeEntity);
        } catch (Exception e) {
            getLogger().warning("Failed to send decoy packet");
        }
    }
}
