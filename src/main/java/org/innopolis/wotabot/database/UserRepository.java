package org.innopolis.wotabot.database;

import org.innopolis.wotabot.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CrudRepository<User, String> {
}
