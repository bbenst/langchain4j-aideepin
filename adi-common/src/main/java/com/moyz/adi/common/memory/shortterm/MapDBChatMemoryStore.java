package com.moyz.adi.common.memory.shortterm;

import com.moyz.adi.common.util.SpringUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;
import static org.mapdb.Serializer.STRING;

/**
 * 基于 MapDB 的短期记忆存储实现。
 */
@Slf4j
public class MapDBChatMemoryStore implements ChatMemoryStore {

    /**
     * 单例对象。
     */
    public static MapDBChatMemoryStore singleton;

    /**
     * MapDB 数据库实例。
     */
    private final DB db;

    /**
     * 内部消息存储映射（memoryId -> JSON）。
     */
    private final Map<String, String> map;

    /**
     * 私有构造函数，禁止外部实例化。
     */
    private MapDBChatMemoryStore() {
        String memoryDir = SpringUtil.getProperty("local.chat-memory");
        log.info("chat memory path:{}", memoryDir);
        db = DBMaker.fileDB(memoryDir + "chat-memory.db").transactionEnable().make();
        map = db.hashMap("messages", STRING, STRING).createOrOpen();
    }

    /**
     * 获取指定会话的消息列表。
     *
     * @param memoryId 会话标识
     * @return 消息列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = map.get((String) memoryId);
        return messagesFromJson(json);
    }

    /**
     * 更新指定会话的消息列表。
     *
     * @param memoryId 会话标识
     * @param messages 消息列表
     * @return 无
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // 首条消息不能为 AiMessage，避免上下文顺序被模型误解
        if (!messages.isEmpty() && messages.get(0) instanceof AiMessage) {
            messages.remove(0);
        }
        if (messages.isEmpty()) {
            return;
        }
        // 只保留可用于上下文的消息类型，避免系统消息散落引发提示词污染
        List<ChatMessage> availableMessage = new ArrayList<>();
        int index = 0;
        if (messages.get(0) instanceof SystemMessage) {
            availableMessage.add(messages.get(0));
            index = 1;
        }
        for (int i = index; i < messages.size(); i++) {
            ChatMessage chatMessage = messages.get(i);
            if (!(chatMessage instanceof SystemMessage)) {
                availableMessage.add(chatMessage);
            }
        }
        String json = messagesToJson(availableMessage);
        map.put((String) memoryId, json);
        db.commit();
    }

    /**
     * 删除指定会话的消息列表。
     *
     * @param memoryId 会话标识
     * @return 无
     */
    @Override
    public void deleteMessages(Object memoryId) {
        map.remove((String) memoryId);
        db.commit();
    }

    /**
     * 获取单例实例。
     *
     * @return 单例对象
     */
    public static MapDBChatMemoryStore getSingleton() {
        if (null == singleton) {
            synchronized (MapDBChatMemoryStore.class) {
                // 双重检查锁降低同步开销，同时保证线程安全
                if (null == singleton) {
                    singleton = new MapDBChatMemoryStore();
                }
            }
        }
        return singleton;
    }
}
