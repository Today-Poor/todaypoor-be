package com.todaypoor.global.response;

import java.util.List;

public record ValidationErrorData(List<ValidationErrorDetail> errors) {
}
