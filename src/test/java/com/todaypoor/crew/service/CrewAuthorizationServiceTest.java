package com.todaypoor.crew.service;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.repository.CrewMemberRepository;
import com.todaypoor.crew.repository.CrewRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CrewAuthorizationServiceTest {

    @Mock
    private CrewRepository crewRepository;

    @Mock
    private CrewMemberRepository crewMemberRepository;

    @InjectMocks
    private CrewAuthorizationService crewAuthorizationService;

    @Test
    void validateMember_success_returnsCrewMember() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CrewMember crewMember = CrewMember.createMember(crewId, userId);

        given(crewRepository.existsByIdAndDeletedAtIsNull(crewId)).willReturn(true);
        given(crewMemberRepository.findByCrewIdAndUserIdAndDeletedAtIsNull(crewId, userId))
                .willReturn(Optional.of(crewMember));

        CrewMember result = crewAuthorizationService.validateMember(crewId, userId);

        assertSame(crewMember, result);
    }

    @Test
    void validateMember_crewNotFound_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(crewRepository.existsByIdAndDeletedAtIsNull(crewId)).willReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewAuthorizationService.validateMember(crewId, userId)
        );

        assertEquals(ErrorCode.CREW_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void validateMember_notCrewMember_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(crewRepository.existsByIdAndDeletedAtIsNull(crewId)).willReturn(true);
        given(crewMemberRepository.findByCrewIdAndUserIdAndDeletedAtIsNull(crewId, userId))
                .willReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewAuthorizationService.validateMember(crewId, userId)
        );

        assertEquals(ErrorCode.CREW_MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void validateOwner_success_returnsCrewMember() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CrewMember owner = CrewMember.createOwner(crewId, userId);

        given(crewRepository.existsByIdAndDeletedAtIsNull(crewId)).willReturn(true);
        given(crewMemberRepository.findByCrewIdAndUserIdAndDeletedAtIsNull(crewId, userId))
                .willReturn(Optional.of(owner));

        CrewMember result = crewAuthorizationService.validateOwner(crewId, userId);

        assertSame(owner, result);
    }

    @Test
    void validateOwner_notOwner_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CrewMember member = CrewMember.createMember(crewId, userId);

        given(crewRepository.existsByIdAndDeletedAtIsNull(crewId)).willReturn(true);
        given(crewMemberRepository.findByCrewIdAndUserIdAndDeletedAtIsNull(crewId, userId))
                .willReturn(Optional.of(member));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewAuthorizationService.validateOwner(crewId, userId)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }
}
