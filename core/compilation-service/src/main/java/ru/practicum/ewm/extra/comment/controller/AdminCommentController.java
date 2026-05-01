package ru.practicum.ewm.extra.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.extra.comment.dto.CommentDto;
import ru.practicum.ewm.extra.comment.dto.UpdateCommentBody;
import ru.practicum.ewm.extra.comment.service.CommentService;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public CommentDto get(@PathVariable @Positive Long commentId) {
        return commentService.get(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(
            @PathVariable @Positive Long commentId,
            @RequestBody @Valid UpdateCommentBody body) {
        log.info("ADMIN: Update comment {}", commentId);
        return commentService.adminUpdate(commentId, body);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long commentId) {
        log.info("ADMIN: Delete comment {}", commentId);
        commentService.adminDelete(commentId);
    }
}
