package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CreateCommentDto;
import ru.practicum.ewm.comment.dto.PrivateUpdateCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.dto.params.CommentParams;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.sharing.BaseService;
import ru.practicum.ewm.sharing.EntityName;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl extends BaseService implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final CommentMapper mapper;

    @Override
    @Transactional
    public CommentDto create(CreateCommentDto dto) {
        User author = findUserOrThrow(dto.authorId());
        Event event = findEventOrThrow(dto.eventId());

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Event %d is not published to be commented".formatted(dto.eventId()));
        }

        Comment comment = mapper.toEntity(dto);
        comment.setAuthor(author);
        comment.setEvent(event);

        comment = commentRepository.save(comment);
        return mapper.toDto(comment);
    }

    @Override
    public CommentDto get(Long id) {
        Comment comment = findCommentOrThrow(id);
        return mapper.toDto(comment);
    }

    @Override
    public List<CommentDto> get(CommentParams params) {
        List<Comment> comments = commentRepository.findByEventId(params.eventId(), params.pageable())
                .getContent();
        return comments.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CommentDto update(UpdateCommentDto dto) {
        Comment comment = findCommentOrThrow(dto.commentId());
        mapper.updateEntity(dto, comment);
        Comment result = commentRepository.save(comment);
        return mapper.toDto(result);
    }

    @Override
    @Transactional
    public CommentDto updatePrivate(PrivateUpdateCommentDto dto) {
        Comment comment = findCommentOrThrow(dto.commentId());
        if (dto.userId() != null &&
            !comment.getAuthor().getId().equals(dto.userId())) {
            throw new ConflictException("You are not the author of this comment");
        }
        mapper.updatePrivateEntity(dto, comment);
        return mapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteByUser(Long userId, Long commentId) {
        Comment comment = findCommentOrThrow(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("You are not the author of this comment");
        }
        commentRepository.delete(comment);
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> throwNotFound(commentId, EntityName.COMMENT));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> throwNotFound(userId, EntityName.USER));
    }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> throwNotFound(eventId, EntityName.EVENT));
    }
}
