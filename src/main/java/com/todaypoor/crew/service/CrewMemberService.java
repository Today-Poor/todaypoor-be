package com.todaypoor.crew.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.todaypoor.crew.dto.crew.request.JoinCrewRequest;
import com.todaypoor.crew.dto.crew.response.JoinCrewResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberDetailResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberListResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberListResponse.CrewMemberList;
import com.todaypoor.crew.entity.Crew;
import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.crew.repository.CrewMemberRepository;
import com.todaypoor.crew.repository.CrewRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.user.entity.User;
import com.todaypoor.user.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewMemberService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewAuthorizationService crewAuthorizationService;
    private final UserRepository userRepository;

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

    public CrewMemberListResponse getCrewMembers(UUID userId, UUID crewId) {

        validateUserId(userId);
        validateCrewId(crewId);

        crewAuthorizationService.validateMember(crewId, userId);

        Crew crew = crewRepository.findByIdAndDeletedAtIsNull(crewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CREW_NOT_FOUND));

        List<CrewMember> crewMembers = crewMemberRepository.findByCrewIdAndDeletedAtIsNull(crewId);
        List<UUID> memberUserIds = crewMembers.stream().map(CrewMember::getUserId).toList();
        Map<UUID, String> nicknameMap = userRepository.findAllById(memberUserIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        List<CrewMemberList> members = crewMembers.stream()
                .map(crewMember -> CrewMemberList.from(crewMember, nicknameMap.get(crewMember.getUserId())))
                .toList();

        return CrewMemberListResponse.of(crew, members);
    }

    private void validateUserId(UUID userId) {
        if (userId == null)  {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
    }

    private void validateCrewId(UUID crewId) {
        if (crewId == null)  {
            throw new IllegalArgumentException("crewId는 필수입니다.");
        }
    }

    public CrewMemberDetailResponse getCrewMemberDetail(UUID userId, UUID crewId, UUID crewMemberId) {

        validateUserId(userId);
        validateCrewId(crewId);
        validateCrewMemberId(crewMemberId);

        crewAuthorizationService.validateMember(crewId, userId);
        CrewMember crewMember = crewAuthorizationService.validateMember(crewId, crewMemberId);

        String nickname = userRepository.findByIdAndDeletedAtIsNull(crewMember.getUserId())
                .map(User::getNickname).orElse(null);
        return CrewMemberDetailResponse.from(crewMember, nickname);
    }

    private void validateCrewMemberId(UUID crewMemberId) {
        if (crewMemberId == null)  {
            throw new IllegalArgumentException("crewMemberId는 필수입니다.");
        }
    }

    @Transactional
    public void leaveCrew(UUID userId, UUID crewId) {

        validateUserId(userId);
        validateCrewId(crewId);

        CrewMember crewMember = crewAuthorizationService.validateMember(crewId, userId);

        // 방장이라면 탈퇴할 수 없음
        if (crewMember.getRole() == CrewRole.OWNER) {
            throw new BusinessException(ErrorCode.OWNER_CANNOT_LEAVE);
        }

        crewMember.softDelete();
        crewMemberRepository.save(crewMember);
    }

    @Transactional
    public void removeCrewMember(UUID userId, UUID crewId, UUID crewMemberId) {

        validateUserId(userId);
        validateCrewId(crewId);
        validateCrewMemberId(crewMemberId);

        // 방장만 강퇴시킬 수 있음
        crewAuthorizationService.validateOwner(crewId, userId);

        // 방장이 자기 자신 강퇴 불가능하도록
        if (userId.equals(crewMemberId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        CrewMember crewMember = crewAuthorizationService.validateMember(crewId, crewMemberId);

        crewMember.softDelete();
        crewMemberRepository.save(crewMember);

    }


}
