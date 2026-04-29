package ru.practicum.ewm.extra.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.dto.EventInternalDto;
import ru.practicum.ewm.api.dto.UserInternalDto;
import ru.practicum.ewm.api.dto.enums.EventStateInternal;
import ru.practicum.ewm.api.sharing.PageableFactory;
import ru.practicum.ewm.extra.client.EventFeignClient;
import ru.practicum.ewm.extra.client.UserFeignClient;
import ru.practicum.ewm.extra.comment.dto.AuthorDto;
import ru.practicum.ewm.extra.comment.dto.CommentDto;
import ru.practicum.ewm.extra.comment.dto.CreateCommentBody;
import ru.practicum.ewm.extra.comment.dto.UpdateCommentBody;
import ru.practicum.ewm.extra.comment.model.Comment;
import ru.practicum.ewm.extra.comment.repository.CommentRepository;
import ru.practicum.ewm.extra.exception.AccessException;
import ru.practicum.ewm.extra.exception.ConflictException;
import ru.practicum.ewm.extra.exception.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventFeignClient eventFeignClient;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    public CommentDto create(Long authorId, Long eventId, CreateCommentBody body) {
        if (!userFeignClient.exists(authorId)) {
            throw new NotFoundException("User %d not found".formatted(authorId));
        }

        EventInternalDto event = eventFeignClient.getEvent(eventId);
        if (event.state() != EventStateInternal.PUBLISHED) {
            throw new ConflictException(
                    "Event %d is not published (state=%s)".formatted(eventId, event.state()));
        }

        Comment comment = Comment.builder()
                .text(body.text())
                .authorId(authorId)
                .eventId(eventId)
                .build();

        Comment saved = commentRepository.save(comment);
        UserInternalDto user = userFeignClient.getUser(authorId);
        return toDto(saved, user);
    }

    @Override
    public CommentDto get(Long commentId) {
        Comment comment = findOrThrow(commentId);
        UserInternalDto user = userFeignClient.getUser(comment.getAuthorId());
        return toDto(comment, user);
    }

    @Override
    public List<CommentDto> getByEvent(Long eventId, Integer from, Integer size) {
        List<Comment> comments = commentRepository
                .findByEventId(eventId, PageableFactory.offset(from, size, Sort.by("id")))
                .getContent();

        if (comments.isEmpty()) return List.of();

        List<Long> authorIds = comments.stream().map(Comment::getAuthorId).distinct().toList();
        Map<Long, UserInternalDto> usersMap = userFeignClient.getUsersBatch(authorIds);

        return comments.stream()
                .map(c -> toDto(c, usersMap.get(c.getAuthorId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto adminUpdate(Long commentId, UpdateCommentBody body) {
        Comment comment = findOrThrow(commentId);
        comment.setText(body.text());
        Comment saved = commentRepository.save(comment);
        return toDto(saved, userFeignClient.getUser(saved.getAuthorId()));
    }

    @Override
    @Transactional
    public void adminDelete(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public CommentDto userUpdate(Long userId, Long commentId, UpdateCommentBody body) {
        Comment comment = findOrThrow(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new AccessException("User %d is not the author of comment %d"
                    .formatted(userId, commentId));
        }
        comment.setText(body.text());
        Comment saved = commentRepository.save(comment);
        UserInternalDto user = userFeignClient.getUser(userId);
        return toDto(saved, user);
    }

    @Override
    @Transactional
    public void userDelete(Long userId, Long commentId) {
        Comment comment = findOrThrow(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new AccessException("User %d is not the author of comment %d"
                    .formatted(userId, commentId));
        }
        commentRepository.delete(comment);
    }


    private CommentDto toDto(Comment c, UserInternalDto user) {
        AuthorDto author = user != null ? new AuthorDto(user.id(), user.name()) : null;
        return new CommentDto(c.getId(), c.getText(), author, c.getCreatedOn(), c.getEventId());
    }

    private Comment findOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment %d not found".formatted(id)));
    }
}
