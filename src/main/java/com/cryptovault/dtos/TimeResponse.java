package com.cryptovault.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeResponse {
    private String dateTime;
    private String timeZone;
}