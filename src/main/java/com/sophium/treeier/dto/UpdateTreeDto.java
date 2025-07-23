package com.sophium.treeier.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTreeDto {
    @Size(max = 255)
    private String title;

    @Size(max = 1000)
    private String description;

    @Size(max = 5)
    private Map<@Size(max = 15) String, @Size(max = 15) String> labels;
}
