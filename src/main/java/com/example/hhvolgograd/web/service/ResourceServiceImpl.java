package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.exception.PatchRequestException;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.model.dto.Entry;
import com.example.hhvolgograd.persistance.db.model.dto.UpdateProperties;
import com.example.hhvolgograd.persistance.db.model.dto.UserUpdates;
import com.example.hhvolgograd.persistance.db.service.CashService;
import com.example.hhvolgograd.validation.patch.CommonValidator;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import static java.lang.String.format;

@Service
@AllArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final CashService service;

    @Override
    public List<User> getUsers(Specification<User> specification, Pageable pageable) {
        return service
                .findAll(specification, pageable)
                .getContent();
    }

    @Override
    public int updateUser(JsonNode patch, long id) {
        val commonValidator = new CommonValidator();
        val updates = new UserUpdates();

        IntStream.range(0, patch.size())
                .mapToObj(patch::get)
                .filter(commonValidator::validate)
                .forEach(jsonNode -> {
                            val fieldName = jsonNode
                                    .path("path")
                                    .textValue()
                                    .replace("/", "");
                            val value = !jsonNode.path("op").textValue().equals("remove")
                                    ? jsonNode.path("value").asText()
                                    : null;

                            updates.set(fieldName, value);
                        }
                );
        requireAtLeastOneIsUpdated(patch, updates);
        return service.updateUser(updates, id);
    }

    private static void requireAtLeastOneIsUpdated(JsonNode patch, UserUpdates updates) {
        boolean hasNoUpdated = updates
                .fieldNames()
                .stream()
                .map((fieldName) -> updates.getGetMethods().get(fieldName).get())
                .noneMatch(UpdateProperties::isUpdated);

        if (hasNoUpdated) {
            throw new PatchRequestException(format("The patch '%s' does not have correct values.", patch));
        }
    }

    @Override
    public void updatePhones(Map<String, String> changes, long userId) {
        val creates = new ArrayList<Entry<String>>();
        val deletes = new ArrayList<Entry<String>>();

        changes.forEach((key, value) -> {
            if (Objects.nonNull(value)) {
                creates.add(new Entry<>("value", value));
            } else {
                deletes.add(new Entry<>("value", key));
            }
        });
        service.updatePhones(creates, deletes, userId);
    }


    @Override
    public void deletePhones(long userId) {
        service.deletePhonesBy(userId);
    }

}
