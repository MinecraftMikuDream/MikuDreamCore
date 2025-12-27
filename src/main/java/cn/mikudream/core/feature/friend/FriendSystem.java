package cn.mikudream.core.feature.friend;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendSystem {
    // 存储结构: 玩家ID -> 好友列表
    private final Map<UUID, List<Friend>> playerFriends = new ConcurrentHashMap<>();

    // 好友请求: 接收者ID -> 请求者列表
    private final Map<UUID, Set<UUID>> pendingRequests = new ConcurrentHashMap<>();

    // 添加好友请求
    public void addFriendRequest(UUID sender, UUID receiver) {
        pendingRequests.computeIfAbsent(receiver, k -> new HashSet<>()).add(sender);
    }

    // 接受好友请求
    public boolean acceptFriendRequest(UUID player, UUID friend) {
        Set<UUID> requests = pendingRequests.get(player);
        if (requests != null && requests.remove(friend)) {
            addFriend(player, friend, "好友");
            addFriend(friend, player, "好友");
            return true;
        }
        return false;
    }

    // 拒绝好友请求
    public boolean denyFriendRequest(UUID player, UUID friend) {
        Set<UUID> requests = pendingRequests.get(player);
        return requests != null && requests.remove(friend);
    }

    // 添加好友
    public void addFriend(UUID playerId, UUID friendId, String friendName) {
        Friend friend = new Friend(playerId, friendId, friendName);
        playerFriends.computeIfAbsent(playerId, k -> new ArrayList<>()).add(friend);
    }

    // 删除好友
    public boolean removeFriend(UUID playerId, UUID friendId) {
        List<Friend> friends = playerFriends.get(playerId);
        if (friends != null) {
            return friends.removeIf(f -> f.getFriendId().equals(friendId));
        }
        return false;
    }

    // 获取好友列表
    public List<Friend> getFriends(UUID playerId) {
        return playerFriends.getOrDefault(playerId, Collections.emptyList());
    }

    // 获取待处理请求
    public Set<UUID> getPendingRequests(UUID playerId) {
        return pendingRequests.getOrDefault(playerId, Collections.emptySet());
    }
}