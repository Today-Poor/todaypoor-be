package com.todaypoor.ranking.mock.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserAmountItem {

    private UUID userId;
    private int totalAmount;
}
