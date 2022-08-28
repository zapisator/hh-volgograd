package com.example.hhvolgograd.persistance.db.repository.custom;

import com.example.hhvolgograd.persistance.db.model.dto.Entry;

import java.util.List;

public interface NativeSqlPhoneRepository {

    int deletePhonesByUserIdAndValues(long UserId, List<Entry<String>> deletes);

    int addPhonesByUserId(long userId, List<Entry<String>> creates);
}
