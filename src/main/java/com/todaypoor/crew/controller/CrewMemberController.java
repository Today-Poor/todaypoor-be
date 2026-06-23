package com.todaypoor.crew.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.todaypoor.crew.dto.crew.request.JoinCrewRequest;
import com.todaypoor.crew.dto.crew.response.JoinCrewResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberDetailResponse;
import com.todaypoor.crew.dto.crewMember.response.CrewMemberListResponse;
import com.todaypoor.crew.service.CrewMemberService;
import com.todaypoor.global.response.ApiResponse;
import com.todaypoor.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "크루 멤버 (Crew Member)", description = "크루 가입, 탈퇴, 강퇴 및 크루원 목록 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/crews")
public class CrewMemberController {

    private final CrewMemberService crewMemberService;

    @Operation(summary = "크루 가입 (참여)", description = "초대 코드를 통해 특정 크루에 신규 멤버로 가입합니다.")
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<JoinCrewResponse>> joinCrew(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody JoinCrewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(crewMemberService.joinCrew(userDetails.getUserId(), request)));
    }

    @Operation(summary = "크루원 목록 조회", description = "지정된 크루에 참여 중인 크루원들의 목록을 가져옵니다.")
    @GetMapping("/{crewId}/members")
    public ResponseEntity<ApiResponse<CrewMemberListResponse>> getCrewMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("crewId") UUID crewId
    ) {
        return ResponseEntity.ok(ApiResponse.success(crewMemberService.getCrewMembers(userDetails.getUserId(), crewId)));
    }

    @Operation(summary = "크루원 상세 정보 조회", description = "특정 크루원의 개인 상태 및 상세 프로필을 가져옵니다.")
    @GetMapping("/{crewId}/members/{userId}")
    public ResponseEntity<ApiResponse<CrewMemberDetailResponse>> getCrewMemberDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("crewId") UUID crewId,
            @PathVariable("userId") UUID crewMemberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(crewMemberService.getCrewMemberDetail(userDetails.getUserId(), crewId, crewMemberId)));
    }

    @Operation(summary = "크루 자진 탈퇴", description = "현재 가입되어 있는 크루에서 스스로 탈퇴합니다.")
    @DeleteMapping("/{crewId}/members/me")
    public ResponseEntity<ApiResponse<Void>> leaveCrew(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("crewId") UUID crewId
    ) {
        crewMemberService.leaveCrew(userDetails.getUserId(), crewId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "크루원 내보내기 (강퇴)", description = "크루 방장이 특정 크루원을 강제 퇴장시킵니다.")
    @DeleteMapping("/{crewId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeCrewMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("crewId") UUID crewId,
            @PathVariable("userId") UUID crewMemberId
    ) {
        crewMemberService.removeCrewMember(userDetails.getUserId(), crewId, crewMemberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
