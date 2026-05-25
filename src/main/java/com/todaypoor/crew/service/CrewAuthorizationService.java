package com.todaypoor.crew.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.todaypoor.crew.entity.CrewMember;
import com.todaypoor.crew.entity.CrewRole;
import com.todaypoor.crew.repository.CrewMemberRepository;
import com.todaypoor.crew.repository.CrewRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CrewAuthorizationService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;

    public CrewMember validateMember(UUID crewId, UUID userId) {
        validateCrewExists(crewId);

        return crewMemberRepository.findByCrewIdAndUserIdAndDeletedAtIsNull(crewId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CREW_MEMBER));
    }

    public CrewMember validateOwner(UUID crewId, UUID userId) {
        CrewMember crewMember = validateMember(crewId, userId);

        if (crewMember.getRole() != CrewRole.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return crewMember;
    }

    private void validateCrewExists(UUID crewId) {
        boolean exists = crewRepository.existsByIdAndDeletedAtIsNull(crewId);
        if (!exists) {
            throw new BusinessException(ErrorCode.CREW_NOT_FOUND);
        }
    }
}
