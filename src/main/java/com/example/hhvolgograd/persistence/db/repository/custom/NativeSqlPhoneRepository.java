package com.example.hhvolgograd.persistence.db.repository.custom;

import com.example.hhvolgograd.persistence.db.model.dto.Entry;

import java.util.List;

public interface NativeSqlPhoneRepository {

    int deletePhonesByUserIdAndValues(long UserId, List<Entry<String>> deletes);

    int addPhonesByUserId(long userId, List<Entry<String>> creates);
}
