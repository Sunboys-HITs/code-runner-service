package ru.mkenopsia.coderunnerservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mkenopsia.coderunnerservice.model.TestEntity;

import java.util.List;

@Repository
public interface TestJpaRepository extends JpaRepository<TestEntity, Long> {

    List<TestEntity> findByTaskId(String taskId);
}
