package com.todaypoor.crew.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

import com.todaypoor.crew.dto.crew.request.CreateCrewRequest;
import com.todaypoor.crew.dto.crew.request.JoinCrewRequest;
import com.todaypoor.crew.dto.crew.request.UpdateCrewRequest;
import com.todaypoor.crew.dto.crew.response.CreateCrewResponse;
import com.todaypoor.crew.dto.crew.response.CrewDetailResponse;
import com.todaypoor.crew.dto.crew.response.CrewMainResponse;
import com.todaypoor.crew.dto.crew.response.InviteCodeResponse;
import com.todaypoor.crew.dto.crew.response.JoinCrewResponse;
import com.todaypoor.crew.dto.crew.response.MyCrewListResponse;
import com.todaypoor.crew.dto.crew.response.UpdateCrewResponse;
import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.crew.repository.CrewMemberRepository;
import com.todaypoor.crew.repository.CrewRepository;
import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;
import com.todaypoor.expense.repository.ExpenseRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class CrewServiceTest {

    @Mock
    private CrewRepository crewRepository;

    @Mock
    private CrewMemberRepository crewMemberRepository;

    @Mock
    private CrewAuthorizationService crewAuthorizationService;

    @Mock
    private ExpenseRepository expenseRepository;

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

    @Test
    void updateCrew_success_preservesDescriptionWhenDescriptionUndefined() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "UPDATE1", LocalDateTime.now().plusDays(1));
        UpdateCrewRequest request = new UpdateCrewRequest(
                "수정된 크루",
                JsonNullable.undefined(),
                4,
                AiMode.COMFORT
        );

        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId)).willReturn(2);
        given(crewRepository.findByIdAndDeletedAtIsNull(crewId)).willReturn(Optional.of(crew));

        UpdateCrewResponse response = crewService.updateCrew(userId, crewId, request);

        assertEquals(crewId, response.crewId());
        assertEquals("수정된 크루", response.name());
        assertEquals("설명", response.description());
        assertEquals(4, response.maxMemberCount());
        assertEquals(2, response.currentMemberCount());
        assertEquals(AiMode.COMFORT, response.aiMode());
        verify(crewAuthorizationService).validateOwner(crewId, userId);
    }

    @Test
    void updateCrew_success_setsDescriptionNullWhenDescriptionPresentNull() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "UPDATE2", LocalDateTime.now().plusDays(1));
        UpdateCrewRequest request = new UpdateCrewRequest(
                null,
                JsonNullable.of(null),
                null,
                null
        );

        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId)).willReturn(2);
        given(crewRepository.findByIdAndDeletedAtIsNull(crewId)).willReturn(Optional.of(crew));

        UpdateCrewResponse response = crewService.updateCrew(userId, crewId, request);

        assertEquals(crewId, response.crewId());
        assertEquals("테스트 크루", response.name());
        assertNull(response.description());
        assertEquals(5, response.maxMemberCount());
        assertEquals(AiMode.ROAST, response.aiMode());
        verify(crewAuthorizationService).validateOwner(crewId, userId);
    }

    @Test
    void updateCrew_noChangedField_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UpdateCrewRequest request = new UpdateCrewRequest(
                null,
                JsonNullable.undefined(),
                null,
                null
        );

        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId)).willReturn(2);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewService.updateCrew(userId, crewId, request)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verifyNoInteractions(crewRepository, crewAuthorizationService);
    }

    @Test
    void updateCrew_maxMemberCountLessThanCurrent_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UpdateCrewRequest request = new UpdateCrewRequest(
                null,
                JsonNullable.undefined(),
                2,
                null
        );

        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId)).willReturn(3);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewService.updateCrew(userId, crewId, request)
        );

        assertEquals(ErrorCode.MAX_MEMBER_COUNT_LESS_THAN_CURRENT, exception.getErrorCode());
        verifyNoInteractions(crewRepository, crewAuthorizationService);
    }

    @Test
    void reissueInviteCode_success_regeneratesInviteCode() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime oldExpiresAt = LocalDateTime.now().plusDays(1);
        Crew crew = crewWithId(crewId, "OLDCODE1", oldExpiresAt);

        given(crewRepository.findByIdAndDeletedAtIsNull(crewId)).willReturn(Optional.of(crew));
        given(crewRepository.existsByInviteCodeAndDeletedAtIsNull(anyString())).willReturn(false);

        InviteCodeResponse response = crewService.reissueInviteCode(userId, crewId);

        assertEquals(crewId, response.crewId());
        assertNotNull(response.inviteCode());
        assertEquals(8, response.inviteCode().length());
        assertTrue(response.inviteCodeExpiresAt().isAfter(oldExpiresAt));
        assertEquals(response.inviteCode(), crew.getInviteCode());
        verify(crewAuthorizationService).validateOwner(crewId, userId);
    }

    @Test
    void getInviteCode_success_returnsActiveInviteCode() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(3);
        Crew crew = crewWithId(crewId, "ACTIVE12", expiresAt);

        given(crewRepository.findByIdAndDeletedAtIsNull(crewId)).willReturn(Optional.of(crew));

        InviteCodeResponse response = crewService.getInviteCode(userId, crewId);

        assertEquals(crewId, response.crewId());
        assertEquals("ACTIVE12", response.inviteCode());
        assertEquals(expiresAt, response.inviteCodeExpiresAt());
        verify(crewAuthorizationService).validateMember(crewId, userId);
    }

    @Test
    void getInviteCode_expiredInviteCode_regeneratesInviteCode() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "EXPIRE12", LocalDateTime.now().plusDays(1));
        setField(crew, "inviteCodeExpiresAt", LocalDateTime.now().minusMinutes(1));

        given(crewRepository.findByIdAndDeletedAtIsNull(crewId)).willReturn(Optional.of(crew));
        given(crewRepository.existsByInviteCodeAndDeletedAtIsNull(anyString())).willReturn(false);

        InviteCodeResponse response = crewService.getInviteCode(userId, crewId);

        assertEquals(crewId, response.crewId());
        assertNotNull(response.inviteCode());
        assertEquals(8, response.inviteCode().length());
        assertTrue(response.inviteCodeExpiresAt().isAfter(LocalDateTime.now()));
        assertEquals(response.inviteCode(), crew.getInviteCode());
        verify(crewAuthorizationService).validateMember(crewId, userId);
    }

    @Test
    void getMyCrews_success_returnsJoinedCrewSummaries() {
        UUID userId = UUID.randomUUID();
        UUID ownerCrewId = UUID.randomUUID();
        UUID memberCrewId = UUID.randomUUID();
        Crew ownerCrew = crewWithId(ownerCrewId, "OWNER111", LocalDateTime.now().plusDays(1));
        Crew memberCrew = crewWithId(memberCrewId, "MEMBER11", LocalDateTime.now().plusDays(1));
        CrewMember ownerMember = CrewMember.createOwner(ownerCrewId, userId);
        CrewMember member = CrewMember.createMember(memberCrewId, userId);

        given(crewMemberRepository.findByUserIdAndDeletedAtIsNull(userId)).willReturn(List.of(ownerMember, member));
        given(crewRepository.findByIdAndDeletedAtIsNull(ownerCrewId)).willReturn(Optional.of(ownerCrew));
        given(crewRepository.findByIdAndDeletedAtIsNull(memberCrewId)).willReturn(Optional.of(memberCrew));
        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(ownerCrewId)).willReturn(1);
        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(memberCrewId)).willReturn(3);

        MyCrewListResponse response = crewService.getMyCrews(userId);

        assertEquals(2, response.crews().size());
        assertEquals(ownerCrewId, response.crews().get(0).crewId());
        assertEquals(CrewRole.OWNER, response.crews().get(0).role());
        assertEquals(1, response.crews().get(0).currentMemberCount());
        assertEquals(memberCrewId, response.crews().get(1).crewId());
        assertEquals(CrewRole.MEMBER, response.crews().get(1).role());
        assertEquals(3, response.crews().get(1).currentMemberCount());
    }

    @Test
    void getCrewDetail_success_returnsCrewDetail() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "DETAIL12", LocalDateTime.now().plusDays(1));

        given(crewRepository.findByIdAndDeletedAtIsNull(crewId)).willReturn(Optional.of(crew));
        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId)).willReturn(2);

        CrewDetailResponse response = crewService.getCrewDetail(userId, crewId);

        assertEquals(crewId, response.crewId());
        assertEquals("테스트 크루", response.name());
        assertEquals("설명", response.description());
        assertEquals(AiMode.ROAST, response.aiMode());
        assertEquals(2, response.currentMemberCount());
        assertEquals(5, response.maxMemberCount());
        assertEquals("DETAIL12", response.inviteCode());
        assertEquals(userId, response.owner().userId());
        verify(crewAuthorizationService).validateMember(crewId, userId);
    }

    @Test
    void deleteCrew_success_softDeletesCrew() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "DELETE12", LocalDateTime.now().plusDays(1));

        given(crewRepository.findByIdAndDeletedAtIsNull(crewId)).willReturn(Optional.of(crew));

        assertFalse(crew.isDeleted());

        crewService.deleteCrew(userId, crewId);

        assertTrue(crew.isDeleted());
        verify(crewAuthorizationService).validateOwner(crewId, userId);
    }

    @Test
    void getCrewMain_success_returnsMemberSummariesWithLatestExpenses() {
        UUID crewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Crew crew = crewWithId(crewId, "MAIN1234", LocalDateTime.now().plusDays(1));
        CrewMember owner = CrewMember.createOwner(crewId, requesterId);
        CrewMember member = CrewMember.createMember(crewId, memberId);
        Expense latestExpense = expenseWithId(
                UUID.randomUUID(),
                memberId,
                crewId,
                ExpenseCategory.CAFE,
                5800,
                LocalDateTime.of(2026, 5, 20, 13, 10)
        );

        given(crewRepository.findByIdAndDeletedAtIsNull(crewId)).willReturn(Optional.of(crew));
        given(crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId)).willReturn(2);
        given(crewMemberRepository.findByCrewIdAndDeletedAtIsNull(crewId)).willReturn(List.of(owner, member));
        given(expenseRepository.findFirstByCrewIdAndUserIdAndDeletedAtIsNullOrderBySpentAtDesc(crewId, requesterId))
                .willReturn(Optional.empty());
        given(expenseRepository.findFirstByCrewIdAndUserIdAndDeletedAtIsNullOrderBySpentAtDesc(crewId, memberId))
                .willReturn(Optional.of(latestExpense));

        CrewMainResponse response = crewService.getCrewMain(requesterId, crewId);

        assertEquals(crewId, response.crewId());
        assertEquals("테스트 크루", response.crewName());
        assertEquals(2, response.currentMemberCount());
        assertEquals(5, response.maxMemberCount());
        assertEquals(2, response.members().size());
        assertEquals(requesterId, response.members().get(0).userId());
        assertEquals(CrewRole.OWNER, response.members().get(0).role());
        assertNull(response.members().get(0).latestExpense());
        assertEquals(memberId, response.members().get(1).userId());
        assertEquals(CrewRole.MEMBER, response.members().get(1).role());
        assertEquals(latestExpense.getId(), response.members().get(1).latestExpense().expenseId());
        assertEquals(ExpenseCategory.CAFE, response.members().get(1).latestExpense().category());
        assertEquals(5800, response.members().get(1).latestExpense().amount());
    }

    @Test
    void getCrewMain_notCrewMember_throwsBusinessException() {
        UUID crewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(crewAuthorizationService.validateMember(crewId, userId))
                .willThrow(new BusinessException(ErrorCode.CREW_MEMBER_NOT_FOUND));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> crewService.getCrewMain(userId, crewId)
        );

        assertEquals(ErrorCode.CREW_MEMBER_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(crewRepository, expenseRepository);
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

    private Expense expenseWithId(
            UUID expenseId,
            UUID userId,
            UUID crewId,
            ExpenseCategory category,
            Integer amount,
            LocalDateTime spentAt
    ) {
        Expense expense = Expense.create(
                userId,
                crewId,
                UUID.randomUUID(),
                category,
                amount,
                "스타벅스",
                "커피",
                ExpenseVisibility.PUBLIC,
                spentAt
        );
        setField(expense, "id", expenseId);
        return expense;
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
