package com.todaypoor.crew.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todaypoor.crew.dto.request.CreateCrewRequest;
import com.todaypoor.crew.dto.request.JoinCrewRequest;
import com.todaypoor.crew.dto.response.CreateCrewResponse;
import com.todaypoor.crew.dto.response.JoinCrewResponse;
import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.crew.repository.CrewMemberRepository;
import com.todaypoor.crew.repository.CrewRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CrewServiceTest {

    @Mock
    private CrewRepository crewRepository;

    @Mock
    private CrewMemberRepository crewMemberRepository;

    @InjectMocks
    private CrewService crewService;

    @Test
    void createCrew_success_savesCrewAndOwnerMember() {
        UUID userId = UUID.randomUUID();
        UUID crewId = UUID.randomUUID();
        CreateCrewRequest request = new CreateCrewRequest("거지방 1조", "설명", 5, AiMode.ROAST);

        given(crewRepository.existsByInviteCodeAndDeletedAtIsNull(anyString())).willReturn(false);
        given(crewRepository.save(any(Crew.class))).willAnswer(invocation -> {
            Crew crew = invocation.getArgument(0);
            setField(crew, "id", crewId);
            return crew;
        });
        given(crewMemberRepository.save(any(CrewMember.class))).willAnswer(invocation -> invocation.getArgument(0));

        CreateCrewResponse response = crewService.createCrew(userId, request);

        assertEquals(crewId, response.crewId());
        assertEquals("거지방 1조", response.name());
        assertEquals("설명", response.description());
        assertEquals(5, response.maxMemberCount());
        assertEquals(1, response.currentMemberCount());
        assertEquals(AiMode.ROAST, response.aiMode());
        assertEquals(userId, response.owner().userId());
        assertNotNull(response.inviteCode());
        assertEquals(8, response.inviteCode().length());
        assertTrue(response.inviteCode().equals(response.inviteCode().toUpperCase()));
        assertNotNull(response.inviteCodeExpiresAt());

        ArgumentCaptor<CrewMember> crewMemberCaptor = ArgumentCaptor.forClass(CrewMember.class);
        verify(crewMemberRepository).save(crewMemberCaptor.capture());

        CrewMember ownerMember = crewMemberCaptor.getValue();
        assertEquals(crewId, ownerMember.getCrewId());
        assertEquals(userId, ownerMember.getUserId());
        assertEquals(CrewRole.OWNER, ownerMember.getRole());
    }

    @Test
    void createCrew_nullUserId_throwsIllegalArgumentException() {
        CreateCrewRequest request = new CreateCrewRequest("거지방 1조", "설명", 5, AiMode.ROAST);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> crewService.createCrew(null, request)
        );

        assertEquals("userId는 필수입니다.", exception.getMessage());
    }

    @Test
    void createCrew_nullRequest_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> crewService.createCrew(userId, null)
        );

        assertEquals("request는 필수입니다.", exception.getMessage());
    }

    @Test
    void joinCrew_success_newMemberCreated() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "ABCD1234", LocalDateTime.now().plusDays(1));

        given(crewRepository.findByInviteCodeAndDeletedAtIsNull("ABCD1234")).willReturn(Optional.of(crew));
        given(crewMemberRepository.existsByCrewIdAndUserIdAndDeletedAtIsNull(crewId, userId)).willReturn(false);
        given(crewMemberRepository.findDeletedMember(crewId, userId)).willReturn(Optional.empty());
        given(crewMemberRepository.save(any(CrewMember.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId)).willReturn(2);

        JoinCrewResponse response = crewService.joinCrew(userId, new JoinCrewRequest(" abcd1234 "));

        assertEquals(crewId, response.crewId());
        assertEquals("테스트 크루", response.crewName());
        assertEquals(CrewRole.MEMBER, response.role());
        assertEquals(2, response.currentMemberCount());
        assertEquals(5, response.maxMemberCount());
        assertNotNull(response.joinedAt());
    }

    @Test
    void joinCrew_success_restoreDeletedMember() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "RESTORE1", LocalDateTime.now().plusDays(1));
        CrewMember deletedMember = CrewMember.createMember(crewId, userId);
        deletedMember.softDelete();

        given(crewRepository.findByInviteCodeAndDeletedAtIsNull("RESTORE1")).willReturn(Optional.of(crew));
        given(crewMemberRepository.existsByCrewIdAndUserIdAndDeletedAtIsNull(crewId, userId)).willReturn(false);
        given(crewMemberRepository.findDeletedMember(crewId, userId)).willReturn(Optional.of(deletedMember));
        given(crewMemberRepository.save(any(CrewMember.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId)).willReturn(2);

        JoinCrewResponse response = crewService.joinCrew(userId, new JoinCrewRequest("restore1"));

        assertEquals(CrewRole.MEMBER, response.role());
        assertFalse(deletedMember.isDeleted());
    }

    @Test
    void joinCrew_crewNotFound_throwsBusinessException() {
        UUID userId = UUID.randomUUID();

        given(crewRepository.findByInviteCodeAndDeletedAtIsNull("NOTFOUND")).willReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewService.joinCrew(userId, new JoinCrewRequest("notfound"))
        );

        assertEquals(ErrorCode.CREW_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void joinCrew_expiredInviteCode_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "EXPIRED1", LocalDateTime.now().plusDays(1));
        setField(crew, "inviteCodeExpiresAt", LocalDateTime.now().minusMinutes(1));

        given(crewRepository.findByInviteCodeAndDeletedAtIsNull("EXPIRED1")).willReturn(Optional.of(crew));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewService.joinCrew(userId, new JoinCrewRequest("expired1"))
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verifyNoInteractions(crewMemberRepository);
    }

    @Test
    void joinCrew_alreadyMember_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "DUPL1234", LocalDateTime.now().plusDays(1));

        given(crewRepository.findByInviteCodeAndDeletedAtIsNull("DUPL1234")).willReturn(Optional.of(crew));
        given(crewMemberRepository.existsByCrewIdAndUserIdAndDeletedAtIsNull(crewId, userId)).willReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewService.joinCrew(userId, new JoinCrewRequest("dupl1234"))
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }

    private Crew crewWithId(UUID crewId, String inviteCode, LocalDateTime expiresAt) {
        Crew crew = Crew.create(
                "테스트 크루",
                "설명",
                inviteCode,
                expiresAt,
                AiMode.ROAST,
                UUID.randomUUID(),
                5
        );
        setField(crew, "id", crewId);
        return crew;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("테스트 필드 주입 실패: " + fieldName, e);
        }
    }
}
