package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.dto.request.UpdateCommentBody;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Admin.COMMENTS)
@Validated
@Slf4j
public class AdminCommentController {
    private final CommentService service;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable @Positive Long commentId) {

        log.info("ADMIN: Delete comment {}", commentId);
        service.delete(commentId);
        log.info("ADMIN: Comment {} deleted", commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(
            @PathVariable @Positive Long commentId,
            @RequestBody @Valid UpdateCommentBody body) {

        log.info("ADMIN: Update comment {}", commentId);
        UpdateCommentDto dto = UpdateCommentDto.of(commentId, body);
        CommentDto result = service.update(dto);
        log.info("ADMIN: Comment {} updated", commentId);
        return result;
    }

    @GetMapping("/{commentId}")
    public CommentDto get(@PathVariable @Positive Long commentId) {

        log.info("PUBLIC: Get comment by id {}", commentId);
        CommentDto result = service.get(commentId);
        log.info("PUBLIC: Found comment {}", result.id());
        return result;
    }
}
