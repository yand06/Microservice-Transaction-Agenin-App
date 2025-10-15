package com.jdt16.agenin.transaction.dto.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LogRequestDTO {
    private String logName;
    private String logDescription;
    private String logType;
}
