package org.innopolis.wotabot.database;

import org.innopolis.wotabot.models.Roommate;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@EnableJpaRepositories
public interface RoommateRepository extends CrudRepository<Roommate, Long> {

}
