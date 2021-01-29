package org.innopolis.wotabot.database;

import org.innopolis.wotabot.models.User;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@EnableJpaRepositories
public interface UserRepository extends CrudRepository<User, String> {

}
