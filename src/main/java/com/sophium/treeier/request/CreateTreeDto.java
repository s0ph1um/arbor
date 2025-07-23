package com.sophium.treeier.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTreeDto {
    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 1000)
    private String description;

    @Size(max = 5)
    private Map<@Size(max = 15) String, @Size(max = 15) String> labels;

    private Long parentTreeId;

    // optional
    private List<CreateTreeNodeDto> nodes;
}
