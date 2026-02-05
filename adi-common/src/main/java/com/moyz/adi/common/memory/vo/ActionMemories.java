package com.moyz.adi.common.memory.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * ActionMemories对象
 */
@Data
public class ActionMemories {
    /**
     * memory
     */
    private List<ActionMemory> memory;

    @Data
    public static class ActionMemory {
        /**
         * 主键ID
         */
        private String id;
        /**
         * text
         */
        private String text;
        /**
         * event
         */
        private String event;
        /**
         * oldMemory
         */
        @JsonProperty("old_memory")
        private String oldMemory;
    }
}
