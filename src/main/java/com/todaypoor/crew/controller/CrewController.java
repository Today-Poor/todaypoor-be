package com.todaypoor.crew.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.todaypoor.crew.dto.request.CreateCrewRequest;
import com.todaypoor.crew.dto.request.JoinCrewRequest;
import com.todaypoor.crew.dto.request.UpdateCrewRequest;
import com.todaypoor.crew.dto.response.CreateCrewResponse;
import com.todaypoor.crew.dto.response.JoinCrewResponse;
import com.todaypoor.crew.dto.response.RegenerateInviteCodeResponse;
import com.todaypoor.crew.dto.response.UpdateCrewResponse;
import com.todaypoor.crew.service.CrewService;
import com.todaypoor.global.response.ApiResponse;

@RestController
@RequestMapping("/api/crews")
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;

    @PostMapping
    public ApiResponse<CreateCrewResponse> createCrew(
            @RequestHeader("X-USER-ID") UUID userId,
            @Valid @RequestBody CreateCrewRequest request
    ) {
        return ApiResponse.success(crewService.createCrew(userId, request));
    }

    @PostMapping("/join")
    public ApiResponse<JoinCrewResponse> joinCrew(
            @RequestHeader("X-USER-ID") UUID userId,
            @Valid @RequestBody JoinCrewRequest request
    ) {
        return ApiResponse.success(crewService.joinCrew(userId, request));
    }

    @PatchMapping("/{crewId}")
    public ApiResponse<UpdateCrewResponse> updateCrew(
            @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable UUID crewId,
            @Valid @RequestBody UpdateCrewRequest request
    ) {
        return ApiResponse.success(crewService.updateCrew(userId, crewId, request));
    }

    @PostMapping("/{crewId}/invite-code/reissue")
    public ApiResponse<RegenerateInviteCodeResponse> reissueInviteCode(
            @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable UUID crewId
    ) {
        return ApiResponse.success(crewService.reissueInviteCode(userId, crewId));
    }

}
