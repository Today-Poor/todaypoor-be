package com.todaypoor.crew.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.todaypoor.crew.dto.request.CreateCrewRequest;
import com.todaypoor.crew.dto.request.JoinCrewRequest;
import com.todaypoor.crew.dto.request.UpdateCrewRequest;
import com.todaypoor.crew.dto.response.CreateCrewResponse;
import com.todaypoor.crew.dto.response.JoinCrewResponse;
import com.todaypoor.crew.dto.response.RegenerateInviteCodeResponse;
import com.todaypoor.crew.dto.response.UpdateCrewResponse;
import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.crew.repository.CrewMemberRepository;
import com.todaypoor.crew.repository.CrewRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewAuthorizationService crewAuthorizationService;

    private static final int INVITE_CODE_EXPIRE_DAYS = 7;
    private static final int INVITE_CODE_LENGTH = 8;
    private static final int INVITE_CODE_MAX_RETRY = 10;

    @Transactional
    public CreateCrewResponse createCrew(UUID userId, CreateCrewRequest request) {

        validateUserId(userId);
        validateCreateCrewRequest(request);

        String inviteCode = generateUniqueInviteCode();
        LocalDateTime inviteCodeExpiresAt = LocalDateTime.now().plusDays(INVITE_CODE_EXPIRE_DAYS);

        Crew crew = Crew.create(
                request.name(),
                request.description(),
                inviteCode,
                inviteCodeExpiresAt,
                request.aiMode(),
                userId,
                request.maxMemberCount()
        );

        Crew savedCrew = crewRepository.save(crew);

        CrewMember ownerMember = CrewMember.createOwner(savedCrew.getId(), userId);
        crewMemberRepository.save(ownerMember);

        Integer currentMemberCount = 1;

        CreateCrewResponse.Owner owner = new CreateCrewResponse.Owner(
                userId,
                null,
                null
        );

        return CreateCrewResponse.of(savedCrew, owner, currentMemberCount);
    }

    private void validateUserId(UUID userId) {
        if (userId == null)  {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
    }

    private void validateCreateCrewRequest(CreateCrewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("name은 필수입니다.");
        }

        if (request.aiMode() == null) {
            throw new IllegalArgumentException("aiMode는 필수입니다.");
        }

        if (request.maxMemberCount() == null) {
            throw new IllegalArgumentException("maxMemberCount는 필수입니다.");
        }
    }

    private String generateUniqueInviteCode() {
        for (int i = 0; i < INVITE_CODE_MAX_RETRY; i++) {
            String code = generateInviteCode();
            if (!crewRepository.existsByInviteCodeAndDeletedAtIsNull(code)) {
                return code;
            }
        }
        throw new IllegalStateException("초대 코드 생성에 실패했습니다.");
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, INVITE_CODE_LENGTH)
                .toUpperCase();
    }

    @Transactional
    public JoinCrewResponse joinCrew(UUID userId, JoinCrewRequest request) {

        validateUserId(userId);
        validateJoinCrewRequest(request);

        String inviteCode = normalizeInviteCode(request.inviteCode());

        Crew crew = crewRepository.findByInviteCodeAndDeletedAtIsNull(inviteCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.CREW_NOT_FOUND));

        if (crew.getInviteCodeExpiresAt() == null || !crew.getInviteCodeExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST); // 만료된 초대코드
        }

        if (crewMemberRepository.existsByCrewIdAndUserIdAndDeletedAtIsNull(crew.getId(), userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST); // 이미 가입된 메머버
        }

        CrewMember crewMember = crewMemberRepository.findDeletedMember(crew.getId(), userId)
                .map(deletedMember -> { // soft delete 된 멤버 값이 있을 때만 수행
                    deletedMember.restoreMember(CrewRole.MEMBER);
                    return crewMemberRepository.save(deletedMember);
                })
                .orElseGet(() -> crewMemberRepository.save(CrewMember.createMember(crew.getId(), userId)));

        Integer currentMemberCount = crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crew.getId());

        return JoinCrewResponse.of(crew, crewMember, currentMemberCount);
    }

    private void validateJoinCrewRequest(JoinCrewRequest request) {
        if (request == null) throw new IllegalArgumentException("request는 필수입니다.");

        if (request.inviteCode() == null || request.inviteCode().isBlank()) {
            throw new IllegalArgumentException("inviteCode는 필수입니다.");
        }
    }

    private String normalizeInviteCode(String inviteCode) {
        return inviteCode.trim().toUpperCase();
    }

    @Transactional
    public UpdateCrewResponse updateCrew(UUID userId, UUID crewId, UpdateCrewRequest request) {

        validateUserId(userId);
        validateCrewId(crewId);

        Integer currentMemberCount = crewMemberRepository.countByCrewIdAndDeletedAtIsNull(crewId);
        validateUpdateCrewRequest(request, currentMemberCount);

        Crew crew = crewRepository.findByIdAndDeletedAtIsNull(crewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CREW_NOT_FOUND));

        String description = crew.getDescription();
        if (request.description().isPresent()) {
            description = request.description().orElse(null);
        }

        // 방장만 수정 가능
        crewAuthorizationService.validateOwner(crewId, userId);

        crew.update(request.name(), description, request.maxMemberCount(), request.aiMode());

        return UpdateCrewResponse.of(crew, currentMemberCount);
    }

    private void validateCrewId(UUID crewId) {
        if (crewId == null)  {
            throw new IllegalArgumentException("crewId는 필수입니다.");
        }
    }

    private void validateUpdateCrewRequest(UpdateCrewRequest request, Integer currentMemberCount) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        boolean hasName = request.name() != null;
        boolean hasDescription = request.description().isPresent();
        boolean hasMaxMemberCount = request.maxMemberCount() != null;
        boolean hasAiMode = request.aiMode() != null;

        // 최소 1개 이상은 변경이 있어야함 -> 아무것도 수정되지 않았다면 예외 발생
        if (!hasName && !hasDescription && !hasMaxMemberCount && !hasAiMode) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 수정할 값이 넘어왔는데, 값이 비어있다면
        if (hasName && request.name().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (hasMaxMemberCount) {
            Integer maxMemberCount = request.maxMemberCount();

            if (maxMemberCount < 1 || maxMemberCount > 5) {
                throw new BusinessException(ErrorCode.INVALID_MAX_MEMBER_COUNT);
            }

            if (maxMemberCount < currentMemberCount) {
                throw new BusinessException(ErrorCode.MAX_MEMBER_COUNT_LESS_THAN_CURRENT);
            }
        }

    }

    @Transactional
    public RegenerateInviteCodeResponse reissueInviteCode(UUID userId, UUID crewId) {

        validateUserId(userId);
        validateCrewId(crewId);

        // 방장만 초대코드 수동 재발급 가능
        crewAuthorizationService.validateOwner(crewId, userId);

        Crew crew = crewRepository.findByIdAndDeletedAtIsNull(crewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CREW_NOT_FOUND));

        String inviteCode = generateUniqueInviteCode();
        LocalDateTime inviteCodeExpiresAt = LocalDateTime.now().plusDays(INVITE_CODE_EXPIRE_DAYS);

        crew.regenerateInviteCode(inviteCode, inviteCodeExpiresAt);

        return RegenerateInviteCodeResponse.from(crew);

    }
}
