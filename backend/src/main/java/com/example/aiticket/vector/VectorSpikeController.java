package com.example.aiticket.vector;

import com.example.aiticket.common.api.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/spike/vector")
public class VectorSpikeController {
    private final VectorSpikeService service;

    public VectorSpikeController(VectorSpikeService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<Void> insert(@RequestBody InsertRequest request) {
        service.insert(request.content(), request.embedding());
        return ApiResponse.ok(null);
    }

    @PostMapping("/search")
    public ApiResponse<List<VectorSpikeRecord>> search(@RequestBody SearchRequest request) {
        return ApiResponse.ok(service.search(request.embedding(), request.limit()));
    }

    public record InsertRequest(
            @NotBlank String content,
            @NotEmpty List<Float> embedding
    ) {
    }

    public record SearchRequest(
            @NotEmpty List<Float> embedding,
            @Min(1) @Max(20) int limit
    ) {
    }
}
