package com.jdt16.agenin.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RestApiResponseError {

    @JsonProperty("error")
    private Map<String, Serializable> restApiResponseRequestError;
}
