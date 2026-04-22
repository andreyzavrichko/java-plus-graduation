package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CreateCommentDto;
import ru.practicum.ewm.comment.dto.PrivateUpdateCommentDto;
import ru.practicum.ewm.comment.dto.params.CommentParams;
import ru.practicum.ewm.comment.dto.request.CreateCommentBody;
import ru.practicum.ewm.comment.dto.request.UpdateCommentBody;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Private.COMMENTS)
@Validated
@Slf4j
public class PrivateCommentController {
    private final CommentService service;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody CreateCommentBody body
    ) {
        log.info("PRIVATE: Create comment '{}' for event {} by user {}", body.text(), eventId, userId);
        CommentDto result = service.create(CreateCommentDto.of(userId, eventId, body));
        log.info("PRIVATE: Comment created with id {}", result.id());
        return result;
    }

    @GetMapping
    public List<CommentDto> getComments(
            @PathVariable("userId") Long userId,
            @PathVariable("eventId") Long eventId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "5") int size
    ) {
        log.info("PRIVATE: Get comments for event {} by user {} from {} size {}", eventId, userId, from, size);
        CommentParams params = CommentParams.of(eventId, from, size);
        List<CommentDto> result = service.get(params);
        log.info("PRIVATE: Get comments completed for event {} by user {}", eventId, userId);
        return result;
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(
            @PathVariable("userId") Long userId,
            @PathVariable("commentId") Long commentId
    ) {
        log.info("PRIVATE: User {} get comment {}", userId, commentId);
        CommentDto result = service.get(commentId);
        log.info("PRIVATE: Found comment {}", result.id());
        return result;
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(
            @PathVariable("userId") Long userId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody UpdateCommentBody body
    ) {
        log.info("PRIVATE: Update comment {} by user {}", commentId, userId);
        CommentDto result = service.updatePrivate(PrivateUpdateCommentDto.of(userId, commentId, body));
        log.info("PRIVATE: Comment {} updated", result.id());
        return result;
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable("userId") Long userId,
            @PathVariable("commentId") Long commentId
    ) {
        log.info("PRIVATE: Delete comment {} by user {}", commentId, userId);
        service.deleteByUser(userId, commentId);
        log.info("PRIVATE: Comment {} deleted", commentId);
    }
}

