package com.todaypoor.crew.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todaypoor.crew.dto.crew.request.JoinCrewRequest;
import com.todaypoor.crew.dto.crew.response.JoinCrewResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberDetailResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberListResponse;
import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.crew.repository.CrewMemberRepository;
import com.todaypoor.crew.repository.CrewRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CrewMemberServiceTest {

    @Mock
    private CrewRepository crewRepository;

    @Mock
    private CrewMemberRepository crewMemberRepository;

    @Mock
    private CrewAuthorizationService crewAuthorizationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CrewMemberService crewMemberService;

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

        JoinCrewResponse response = crewMemberService.joinCrew(userId, new JoinCrewRequest(" abcd1234 "));

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

        JoinCrewResponse response = crewMemberService.joinCrew(userId, new JoinCrewRequest("restore1"));

        assertEquals(CrewRole.MEMBER, response.role());
        assertFalse(deletedMember.isDeleted());
    }

    @Test
    void joinCrew_crewNotFound_throwsBusinessException() {
        UUID userId = UUID.randomUUID();

        given(crewRepository.findByInviteCodeAndDeletedAtIsNull("NOTFOUND")).willReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewMemberService.joinCrew(userId, new JoinCrewRequest("notfound"))
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
                () -> crewMemberService.joinCrew(userId, new JoinCrewRequest("expired1"))
        );

        assertEquals(ErrorCode.EXPIRED_INVITE_CODE, exception.getErrorCode());
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
                () -> crewMemberService.joinCrew(userId, new JoinCrewRequest("dupl1234"))
        );

        assertEquals(ErrorCode.ALREADY_JOINED_CREW, exception.getErrorCode());
    }

    @Test
    void joinCrew_limitExceeded_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "LIMIT123", LocalDateTime.now().plusDays(1));

        given(crewRepository.findByInviteCodeAndDeletedAtIsNull("LIMIT123")).willReturn(Optional.of(crew));
        given(crewMemberRepository.existsByCrewIdAndUserIdAndDeletedAtIsNull(crewId, userId)).willReturn(false);
        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId)).willReturn(5);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewMemberService.joinCrew(userId, new JoinCrewRequest("limit123"))
        );

        assertEquals(ErrorCode.CREW_MEMBER_LIMIT_EXCEEDED, exception.getErrorCode());
        verify(crewMemberRepository).countByCrewIdAndDeletedAtIsNull(crewId);
    }

    @Test
    void getCrewMembers_success_returnsMemberList() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "LIST1234", LocalDateTime.now().plusDays(1));
        CrewMember member1 = CrewMember.createOwner(crewId, userId);
        CrewMember member2 = CrewMember.createMember(crewId, UUID.randomUUID());

        given(crewRepository.findByIdAndDeletedAtIsNull(crewId)).willReturn(Optional.of(crew));
        given(crewMemberRepository.findByCrewIdAndDeletedAtIsNull(crewId)).willReturn(List.of(member1, member2));
        given(userRepository.findAllById(anyList())).willReturn(List.of());

        CrewMemberListResponse response = crewMemberService.getCrewMembers(userId, crewId);

        assertEquals(2, response.members().size());
        assertEquals(userId, response.members().get(0).userId());
        assertEquals(CrewRole.OWNER, response.members().get(0).role());
        verify(crewAuthorizationService).validateMember(crewId, userId);
    }

    @Test
    void getCrewMemberDetail_success_returnsMemberDetail() {
        UUID crewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        CrewMember targetMember = CrewMember.createMember(crewId, targetUserId);
        CrewMember requesterMember = CrewMember.createMember(crewId, requesterId);

        given(crewAuthorizationService.validateMember(crewId, requesterId)).willReturn(requesterMember);
        given(crewAuthorizationService.validateMember(crewId, targetUserId)).willReturn(targetMember);
        given(userRepository.findByIdAndDeletedAtIsNull(targetUserId)).willReturn(Optional.empty());

        CrewMemberDetailResponse response = crewMemberService.getCrewMemberDetail(requesterId, crewId, targetUserId);

        assertEquals(targetUserId, response.userId());
        assertEquals(CrewRole.MEMBER, response.role());
        verify(crewAuthorizationService).validateMember(crewId, requesterId);
        verify(crewAuthorizationService).validateMember(crewId, targetUserId);
    }

    @Test
    void leaveCrew_success_softDeletesMember() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CrewMember member = CrewMember.createMember(crewId, userId);

        given(crewAuthorizationService.validateMember(crewId, userId)).willReturn(member);

        assertFalse(member.isDeleted());

        crewMemberService.leaveCrew(userId, crewId);

        assertTrue(member.isDeleted());
    }

    @Test
    void leaveCrew_ownerCannotLeave_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CrewMember owner = CrewMember.createOwner(crewId, userId);

        given(crewAuthorizationService.validateMember(crewId, userId)).willReturn(owner);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewMemberService.leaveCrew(userId, crewId)
        );

        assertEquals(ErrorCode.OWNER_CANNOT_LEAVE, exception.getErrorCode());
        assertFalse(owner.isDeleted());
    }

    @Test
    void removeCrewMember_success_softDeletesTargetMember() {
        UUID crewId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID targetMemberId = UUID.randomUUID();
        CrewMember targetMember = CrewMember.createMember(crewId, targetMemberId);

        given(crewAuthorizationService.validateMember(crewId, targetMemberId)).willReturn(targetMember);

        assertFalse(targetMember.isDeleted());

        crewMemberService.removeCrewMember(ownerId, crewId, targetMemberId);

        assertTrue(targetMember.isDeleted());
        verify(crewAuthorizationService).validateOwner(crewId, ownerId);
        verify(crewAuthorizationService).validateMember(crewId, targetMemberId);
    }

    @Test
    void removeCrewMember_selfRemove_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewMemberService.removeCrewMember(ownerId, crewId, ownerId)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(crewAuthorizationService).validateOwner(crewId, ownerId);
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
