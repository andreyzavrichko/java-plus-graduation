package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.dto.EventInternalDto;
import ru.practicum.ewm.api.dto.enums.EventStateInternal;
import ru.practicum.ewm.request.client.EventFeignClient;
import ru.practicum.ewm.request.client.UserFeignClient;
import ru.practicum.ewm.request.dto.*;
import ru.practicum.ewm.request.exception.*;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserFeignClient userFeignClient;
    private final EventFeignClient eventFeignClient;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        if (!userFeignClient.exists(userId)) {
            throw new NotFoundException("User %d not found".formatted(userId));
        }

        EventInternalDto event = eventFeignClient.getEvent(eventId);

        if (event.state() != EventStateInternal.PUBLISHED) {
            throw new ConflictException(
                    "Cannot participate: event %d is not published (state=%s)"
                            .formatted(eventId, event.state()));
        }
        if (event.initiatorId().equals(userId)) {
            throw new ConflictException(
                    "User %d cannot participate in their own event %d"
                            .formatted(userId, eventId));
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException(
                    "User %d already has a request for event %d"
                            .formatted(userId, eventId));
        }

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = event.participantLimit();

        if (limit > 0 && !event.requestModeration() && confirmed >= limit) {
            throw new ConflictException(
                    "Event %d has reached participant limit (%d/%d)"
                            .formatted(eventId, confirmed, limit));
        }

        RequestStatus status = (!event.requestModeration() || limit == 0)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        ParticipationRequest req = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .eventId(eventId)
                .requesterId(userId)
                .status(status)
                .build();

        return requestMapper.toDto(requestRepository.save(req));
    }

    @Override
    public List<ParticipationRequestDto> get(Long userId) {
        if (!userFeignClient.exists(userId)) {
            throw new NotFoundException("User %d not found".formatted(userId));
        }
        return requestRepository.findAllByRequesterId(userId)
                .stream().map(requestMapper::toDto).toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        ParticipationRequest req = findOrThrow(requestId);
        if (!req.getRequesterId().equals(userId)) {
            throw new AccessException(
                    "User %d is not requester of request %d".formatted(userId, requestId));
        }
        req.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(req));
    }


    @Override
    public List<ParticipationRequestDto> getByEvent(Long eventId) {
        return requestRepository.findAllByEventId(eventId)
                .stream().map(requestMapper::toDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatuses(
            Long userId, Long eventId, Integer participantLimit,
            EventRequestStatusUpdateRequest body) {

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = participantLimit;

        List<ParticipationRequest> requests = requestRepository.findAllById(body.requestIds());
        List<ParticipationRequest> toConfirm = new ArrayList<>();
        List<ParticipationRequest> toReject  = new ArrayList<>();

        for (ParticipationRequest req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException(
                        "Request %d is not PENDING (current: %s)"
                                .formatted(req.getId(), req.getStatus()));
            }
            if (body.status() == RequestStatus.CONFIRMED) {
                if (limit > 0 && confirmed >= limit) {
                    throw new ConflictException(
                            "Participant limit reached for event %d".formatted(eventId));
                }
                req.setStatus(RequestStatus.CONFIRMED);
                toConfirm.add(req);
                confirmed++;
            } else if (body.status() == RequestStatus.REJECTED) {
                req.setStatus(RequestStatus.REJECTED);
                toReject.add(req);
            }
        }
        requestRepository.saveAll(requests);

        return new EventRequestStatusUpdateResult(
                toConfirm.stream().map(requestMapper::toDto).toList(),
                toReject.stream().map(requestMapper::toDto).toList());
    }

    @Override
    public Map<Long, Long> confirmedCounts(List<Long> eventIds) {
        return requestRepository.getConfirmedRequestsCounts(eventIds);
    }


    private ParticipationRequest findOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request %d not found".formatted(id)));
    }
}