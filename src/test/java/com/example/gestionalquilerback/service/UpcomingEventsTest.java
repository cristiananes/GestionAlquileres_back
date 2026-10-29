package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.model.entity.CalendarEvent;
import com.example.gestionalquilerback.repository.CalendarEventRepository;
import com.example.gestionalquilerback.security.SecurityUtil;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class UpcomingEventsTest {
    @Test
    void showsTheFiveNearestEventsForTheCurrentUserWithoutDeletingSimilarEvents() {
        var repository = mock(CalendarEventRepository.class);
        var security = mock(SecurityUtil.class);
        when(security.getCurrentUserId()).thenReturn(7L);
        var now = LocalDateTime.now();
        var records = LongStream.iterate(7, n -> n - 1).limit(7)
                .mapToObj(id -> CalendarEvent.builder().id(id).title("Fin de contrato")
                        .startDateTime(now.plusDays(id)).build()).toList();
        when(repository.findByUserIdAndStartDateTimeAfter(eq(7L), any())).thenReturn(records);
        var service = new CalendarEventService(repository, mock(PropertyService.class), security);
        var result = service.findUpcoming();
        assertEquals(5, result.size());
        assertEquals(1L, result.getFirst().getId());
        assertEquals(5L, result.getLast().getId());
        verify(repository, never()).findByStartDateTimeAfter(any());
    }
}
