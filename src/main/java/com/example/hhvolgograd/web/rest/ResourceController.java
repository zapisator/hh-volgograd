package com.example.hhvolgograd.web.rest;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.web.service.ResourceService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/resource")
@AllArgsConstructor
@Tag(
        name = "User resources",
        description = "List users by criterion, change ones data"
)
public class ResourceController {

    private final ResourceService service;

    @GetMapping("/users")
    @Operation(summary = "list users. One may filter, sort, and page the result")
    public List<User> list(
            @Parameter(
                    description =
                            "Filter parameters as defined at https://github.com/turkraft/spring-filter. May be absent."
                                    + "\nExample: /resource/users?filter= id:5 or (age > 20 and age < 50)",
                    schema = @Schema(type = "string")
            )
            @Filter
            @Nullable
            Specification<User> specification,

            @ParameterObject
            @PageableDefault(size = 3)
            Pageable pageable
    ) {
        return service.getUsers(specification, pageable);
    }

}
