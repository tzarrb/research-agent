package com.ivan.researchagent.common.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Copyright (c) 2024 Ivan, Inc.
 * All Rights Reserved.
 * Choice Proprietary and Confidential.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2024/12/03 17:26
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoleMessage implements Serializable {

    @JsonPropertyDescription("对话角色，如:user、assistant、system")
    private String role;

    @JsonPropertyDescription("对话内容")
    private String content;

    @JsonPropertyDescription("对话的媒体链接")
    private List<String> mediaUrls;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoleMessage that = (ChatRoleMessage) o;
        return role.equals(that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role);
    }
}
