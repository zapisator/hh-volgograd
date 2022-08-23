package com.example.hhvolgograd.web.rest;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.web.service.ResourceService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class ResourceController {

    private final ResourceService service;

    @GetMapping("/users")
    @Operation(summary = "list users. One may filter, sort, and page the result")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<User>> list(
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
        val users = service.getUsers(specification, pageable);

        return ResponseEntity.ok(users);
    }

    @PatchMapping(value = "/user/{id}/updating", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "updates user. Path id and grant scope id must match")
    @SecurityRequirement(name = "Bearer Authentication")
    @Parameter(in = ParameterIn.PATH, name = "id", schema = @Schema(type = "integer"))
    public ResponseEntity<String> updateUser(
            @RequestBody
            JsonNode patch,
            @PathVariable int id
    ) {
        service.updateUser(patch, id);
        return ResponseEntity
                .ok("The user is updated.");
    }

}
