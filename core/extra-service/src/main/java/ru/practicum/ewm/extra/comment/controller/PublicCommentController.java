package ru.practicum.ewm.extra.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.extra.comment.dto.CommentDto;
import ru.practicum.ewm.extra.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
@Validated
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
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
}