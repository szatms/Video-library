package io.github.szatms.videolibrary.settings.appsettings;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AppSettingsRepository extends MongoRepository<AppSettings, String> {
    Optional<AppSettings> getById(String id);
    List<AppSettings> findAll();
}
