package org.innopolis.wotabot.database;

import org.innopolis.wotabot.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserRepository extends CrudRepository<User, String> {

}
