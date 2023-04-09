package me.amasiero.library.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Book {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String author;
}
