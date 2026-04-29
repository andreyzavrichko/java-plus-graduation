package ru.practicum.ewm.extra.comment.service;

import ru.practicum.ewm.extra.comment.dto.CommentDto;
import ru.practicum.ewm.extra.comment.dto.CreateCommentBody;
import ru.practicum.ewm.extra.comment.dto.UpdateCommentBody;

import java.util.List;

public interface CommentService {
    CommentDto create(Long authorId, Long eventId, CreateCommentBody body);

    CommentDto get(Long commentId);

    List<CommentDto> getByEvent(Long eventId, Integer from, Integer size);

    CommentDto adminUpdate(Long commentId, UpdateCommentBody body);

    void adminDelete(Long commentId);

    CommentDto userUpdate(Long userId, Long commentId, UpdateCommentBody body);

    void userDelete(Long userId, Long commentId);
}
