package com.github.solairerove.darkkitchen.api.common.dto;

import java.util.List;

public record ErrorResponse(String error, String code, List<String> details) {
}
