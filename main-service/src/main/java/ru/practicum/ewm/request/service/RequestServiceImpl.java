package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.AccessException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.sharing.BaseService;
import ru.practicum.ewm.sharing.EntityName;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.request.model.RequestStatus.CANCELED;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl extends BaseService implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {

        User user = findUserOrThrow(userId);
        Event event = findEventOrThrow(eventId);

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException(
                    "Cannot participate in event %d: event is not published (current state: %s)"
                            .formatted(eventId, event.getState())
            );
        }

        if (requestRepository.existsByEventAndRequester(event, user)) {
            throw new ConflictException(
                    "User %d already has a participation request for event %d"
                            .formatted(user.getId(), eventId)
            );
        }

        if (event.getInitiator().equals(user)) {
            throw new ConflictException(
                    "User %d cannot participate in their own event %d"
                            .formatted(user.getId(), eventId)
            );
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() > 0
                && !event.getRequestModeration()
                && event.getParticipantLimit() <= confirmedRequests) {
            throw new ConflictException(
                    "Event %d has reached participant limit (%d/%d)"
                            .formatted(eventId, confirmedRequests, event.getParticipantLimit())
            );
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);

        RequestStatus status = (!event.getRequestModeration() || event.getParticipantLimit() == 0)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        request.setStatus(status);

        ParticipationRequest saved = requestRepository.save(request);

        return requestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> get(Long userId) {
        User user = findUserOrThrow(userId);

        return requestRepository.findAllByRequester(user).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        User user = findUserOrThrow(userId);

        ParticipationRequest request = findRequestOrThrow(requestId);

        if (!request.getRequester().equals(user)) {
            throw new AccessException(
                    "User %d attempted to cancel request %d, but is not the requester"
                            .formatted(userId, requestId)
            );
        }

        request.setStatus(CANCELED);
        ParticipationRequest savedRequest = requestRepository.save(request);

        return requestMapper.toDto(savedRequest);
    }

    private ParticipationRequest findRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> throwNotFound(requestId, EntityName.REQUEST));
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