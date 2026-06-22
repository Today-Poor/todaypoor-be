package com.todaypoor.crew.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.todaypoor.crew.dto.crew.request.CreateCrewRequest;
import com.todaypoor.crew.dto.crew.request.UpdateCrewRequest;
import com.todaypoor.crew.dto.crew.response.CreateCrewResponse;
import com.todaypoor.crew.dto.crew.response.CrewDetailResponse;
import com.todaypoor.crew.dto.crew.response.CrewMainResponse;
import com.todaypoor.crew.dto.crew.response.InviteCodeResponse;
import com.todaypoor.crew.dto.crew.response.MyCrewListResponse;
import com.todaypoor.crew.dto.crew.response.UpdateCrewResponse;
import com.todaypoor.crew.service.CrewService;
import com.todaypoor.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "크루 (Crew)", description = "크루 생성, 관리, 초대코드 발급 및 삭제 API")
@RestController
@RequestMapping("/api/crews")
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;

    @Operation(summary = "크루 생성", description = "새로운 소비 아끼기 크루를 개설합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCrewResponse>> createCrew(
            @Parameter(description = "사용자 고유 식별 ID (UUID)") @RequestHeader("X-USER-ID") UUID userId,
            @Valid @RequestBody CreateCrewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(crewService.createCrew(userId, request)));
    }

    @Operation(summary = "크루 수정", description = "크루명 및 변경할 한도 등을 업데이트합니다.")
    @PatchMapping("/{crewId}")
    public ResponseEntity<ApiResponse<UpdateCrewResponse>> updateCrew(
            @Parameter(description = "사용자 고유 식별 ID (UUID)") @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable UUID crewId,
            @Valid @RequestBody UpdateCrewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(crewService.updateCrew(userId, crewId, request)));
    }

    @Operation(summary = "초대 코드 재발급", description = "크루 진입을 위한 초대 코드를 재생성합니다.")
    @PostMapping("/{crewId}/invite-code/reissue")
    public ResponseEntity<ApiResponse<InviteCodeResponse>> reissueInviteCode(
            @Parameter(description = "사용자 고유 식별 ID (UUID)") @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable UUID crewId
    ) {
        return ResponseEntity.ok(ApiResponse.success(crewService.reissueInviteCode(userId, crewId)));
    }

    @Operation(summary = "초대 코드 조회", description = "크루원 초대를 위한 기존 초대 코드를 획득합니다.")
    @PostMapping("/{crewId}/invite-code")
    public ResponseEntity<ApiResponse<InviteCodeResponse>> getInviteCode(
            @Parameter(description = "사용자 고유 식별 ID (UUID)") @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable UUID crewId
    ) {
        return ResponseEntity.ok(ApiResponse.success(crewService.getInviteCode(userId, crewId)));
    }

    @Operation(summary = "내가 가입한 크루 목록 조회", description = "현재 사용자가 참여 중이거나 생성한 모든 크루 목록을 가져옵니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<MyCrewListResponse>> getMyCrews(
            @Parameter(description = "사용자 고유 식별 ID (UUID)") @RequestHeader("X-USER-ID") UUID userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(crewService.getMyCrews(userId)));
    }

    @Operation(summary = "크루 상세 정보 조회", description = "지정된 크루의 상태 및 상세 정보를 반환합니다.")
    @GetMapping("/{crewId}/detail")
    public ResponseEntity<ApiResponse<CrewDetailResponse>> getCrewDetail(
            @Parameter(description = "사용자 고유 식별 ID (UUID)") @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable UUID crewId
    ) {
        return ResponseEntity.ok(ApiResponse.success(crewService.getCrewDetail(userId, crewId)));
    }

    @Operation(summary = "크루 삭제", description = "생성한 크루를 영구 폐쇄/삭제합니다 (소프트 딜리트).")
    @DeleteMapping("/{crewId}")
    public ResponseEntity<ApiResponse<Void>> deleteCrew(
            @Parameter(description = "사용자 고유 식별 ID (UUID)") @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable UUID crewId
    ) {
        crewService.deleteCrew(userId, crewId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "크루 메인 화면 정보 조회", description = "크루원들의 소비 현황 등 크루 내 메인보드 정보를 제공합니다.")
    @GetMapping("/{crewId}")
    public ResponseEntity<ApiResponse<CrewMainResponse>> getCrewMain(
            @Parameter(description = "사용자 고유 식별 ID (UUID)") @RequestHeader("X-USER-ID") UUID userId,
            @PathVariable UUID crewId
    ) {
        return ResponseEntity.ok(ApiResponse.success(crewService.getCrewMain(userId, crewId)));
    }

}
