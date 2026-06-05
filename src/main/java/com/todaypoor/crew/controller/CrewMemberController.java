package com.todaypoor.crew.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.todaypoor.crew.dto.crewMember.response.CrewMemberDetailResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberListResponse;
import com.todaypoor.crew.service.CrewMemberService;
import com.todaypoor.global.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/crews")
public class CrewMemberController {

    private final CrewMemberService crewMemberService;

    @GetMapping("/{crewId}/members")
    public ApiResponse<CrewMemberListResponse> getCrewMembers(
            @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable("crewId") UUID crewId
    ) {
        return ApiResponse.success(crewMemberService.getCrewMembers(userId, crewId));
    }

    @GetMapping("/{crewId}/members/{userId}")
    public ApiResponse<CrewMemberDetailResponse> getCrewMemberDetail(
            @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable("crewId") UUID crewId,
            @PathVariable("userId") UUID crewMemberId
    ) {
        return ApiResponse.success(crewMemberService.getCrewMemberDetail(userId, crewId, crewMemberId));
    }

}
