package com.todaypoor.crew.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewMemberService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewAuthorizationService crewAuthorizationService;

    public CrewMemberListResponse getCrewMembers(UUID userId, UUID crewId) {

        validateUserId(userId);
        validateCrewId(crewId);

        crewAuthorizationService.validateMember(crewId, userId);

        Crew crew = crewRepository.findByIdAndDeletedAtIsNull(crewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CREW_NOT_FOUND));

        List<CrewMemberList> members = crewMemberRepository.findByCrewIdAndDeletedAtIsNull(crewId).stream()
                .map(crewMember -> {
                    return CrewMemberList.from(crewMember);
                })
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
        crewAuthorizationService.validateMember(crewId, crewMemberId);

        CrewMember crewMember = crewMemberRepository.findByCrewIdAndUserIdAndDeletedAtIsNull(crewId, crewMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CREW_MEMBER_NOT_FOUND));

        return CrewMemberDetailResponse.from(crewMember);
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
    }




}
