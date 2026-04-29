package ru.practicum.ewm.extra.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.extra.comment.dto.CommentDto;
import ru.practicum.ewm.extra.comment.dto.CreateCommentBody;
import ru.practicum.ewm.extra.comment.dto.UpdateCommentBody;
import ru.practicum.ewm.extra.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody CreateCommentBody body) {
        log.info("PRIVATE: Create comment for event {} by user {}", eventId, userId);
        return commentService.create(userId, eventId, body);
    }

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getByEvent(
            @PathVariable @Positive Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "5") @Positive Integer size) {
        return commentService.getByEvent(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto get(
            @PathVariable @Positive Long commentId) {
        return commentService.get(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId,
            @Valid @RequestBody UpdateCommentBody body) {
        log.info("PRIVATE: Update comment {} by user {}", commentId, userId);
        return commentService.userUpdate(userId, commentId, body);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long commentId) {
        log.info("PRIVATE: Delete comment {} by user {}", commentId, userId);
        commentService.userDelete(userId, commentId);
    }
}
