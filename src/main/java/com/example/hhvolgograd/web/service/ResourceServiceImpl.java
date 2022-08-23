package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.service.CashService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void updateUser(JsonNode patch, long id) {
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
                            val value = jsonNode.path("op").textValue().equals("remove")
                                    ? jsonNode.path("value").textValue()
                                    : null;

                            updates.set(fieldName, value);
                        }
                );
        service.updateUser(updates, id);
    }

    @Override
    public void updatePhones(JsonNode patch, long userId) {

    }

    @Override
    public void deletePhones(long userId) {

    }

}
