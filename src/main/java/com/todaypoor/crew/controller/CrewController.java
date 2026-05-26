package com.todaypoor.crew.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.todaypoor.crew.dto.request.CreateCrewRequest;
import com.todaypoor.crew.dto.request.JoinCrewRequest;
import com.todaypoor.crew.dto.response.CreateCrewResponse;
import com.todaypoor.crew.dto.response.JoinCrewResponse;
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

}
