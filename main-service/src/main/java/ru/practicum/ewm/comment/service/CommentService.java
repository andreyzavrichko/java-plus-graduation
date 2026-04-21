package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CreateCommentDto;
import ru.practicum.ewm.comment.dto.PrivateUpdateCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.dto.params.CommentParams;

import java.util.List;

public interface CommentService {

    CommentDto create(CreateCommentDto dto);

    CommentDto get(Long id);

    List<CommentDto> get(CommentParams params);

    void delete(Long id);

    CommentDto update(UpdateCommentDto dto);

    CommentDto updatePrivate(PrivateUpdateCommentDto dto);

    void deleteByUser(Long userId, Long commentId);
}
