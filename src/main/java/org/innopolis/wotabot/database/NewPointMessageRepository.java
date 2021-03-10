package org.innopolis.wotabot.database;

import org.innopolis.wotabot.models.NewPointMessage;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@EnableJpaRepositories
public interface NewPointMessageRepository extends CrudRepository<NewPointMessage, Integer> {
}
