package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.Guard;
import java.time.Instant;
import java.util.Objects;

/** 评论：挂在问题或回答下，支持一层“二级回复”。 */
public class Comment {

    private Long id;
    private final TargetType targetType;
    private final long targetId;
    private final long questionId;
    private final long authorId;
    private final Long parentId;
    private final String body;
    private final Instant createdAt;

    private Comment(Long id, TargetType targetType, long targetId, long questionId, long authorId, Long parentId,
                    String body, Instant createdAt) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.questionId = questionId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.body = body;
        this.createdAt = createdAt;
    }

    public static final int MAX_LENGTH = 600;
    /** 下限 2 字：单字评论（「好」「顶」）没有信息量。指导书 V2 第 3.7 节。 */
    public static final int MIN_LENGTH = 2;

    public static Comment post(TargetType targetType, long targetId, long questionId, long authorId, Comment parent,
                               String body, Instant now) {
        String text = Guard.requireText(body, MIN_LENGTH, MAX_LENGTH, "QA-1003", "评论");
        Long parentId = null;
        if (parent != null) {
            if (parent.parentId != null) {
                throw new BusinessRuleException("QA-1016", "评论回复只支持一层，不能再回复二级评论");
            }
            if (parent.targetType != targetType || parent.targetId != targetId) {
                throw new BusinessRuleException("QA-1017", "回复必须与被回复的评论挂在同一内容下");
            }
            parentId = Objects.requireNonNull(parent.id, "被回复的评论尚未分配 ID");
        }
        return new Comment(null, targetType, targetId, questionId, authorId, parentId, text, now);
    }

    public static Comment reconstitute(Long id, TargetType targetType, long targetId, long questionId, long authorId,
                                       Long parentId, String body, Instant createdAt) {
        return new Comment(id, targetType, targetId, questionId, authorId, parentId, body, createdAt);
    }

    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID 已分配");
        }
        this.id = id;
    }

    public Long id() { return id; }
    public TargetType targetType() { return targetType; }
    public long targetId() { return targetId; }
    public long questionId() { return questionId; }
    public long authorId() { return authorId; }
    public Long parentId() { return parentId; }
    public String body() { return body; }
    public Instant createdAt() { return createdAt; }
}
