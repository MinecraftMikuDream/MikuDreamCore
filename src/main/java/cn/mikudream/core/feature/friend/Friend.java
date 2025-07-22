package cn.mikudream.core.feature.friend;

import java.util.UUID;

public class Friend {
    private final UUID playerId;
    private final UUID friendId;
    private String friendName;
    private String remark;
    private String group = "默认分组";

    public Friend(UUID playerId, UUID friendId, String friendName) {
        this.playerId = playerId;
        this.friendId = friendId;
        this.friendName = friendName;
    }

    // Getters and Setters
    public UUID getPlayerId() { return playerId; }
    public UUID getFriendId() { return friendId; }
    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    @Override
    public String toString() {
        return remark != null ? remark : friendName;
    }
}