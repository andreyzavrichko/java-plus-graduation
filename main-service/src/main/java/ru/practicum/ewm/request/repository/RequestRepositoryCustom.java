package ru.practicum.ewm.request.repository;

import java.util.List;
import java.util.Map;

public interface RequestRepositoryCustom {

    Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds);
}
