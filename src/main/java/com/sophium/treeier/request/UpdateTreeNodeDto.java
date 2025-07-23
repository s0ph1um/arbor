package com.sophium.treeier.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTreeNodeDto {
    private String title;
    private String description;
    private Boolean flagValue;
    private String linkValue;
}
