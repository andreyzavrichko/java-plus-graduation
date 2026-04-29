package ru.practicum.ewm.user.dto;
import java.util.List;
public record UserSearchRequest(List<Long> ids, Integer from, Integer size) {}
