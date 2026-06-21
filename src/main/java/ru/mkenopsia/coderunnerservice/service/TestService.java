package ru.mkenopsia.coderunnerservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mkenopsia.coderunnerservice.model.TestEntity;
import ru.mkenopsia.coderunnerservice.storage.TestJpaRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestJpaRepository repository;

    @Transactional
    public TestEntity add(UUID taskId, String inputData, String expectedOutput) {
        return repository.save(TestEntity.builder()
                .taskId(taskId)
                .inputData(inputData)
                .expectedOutput(expectedOutput)
                .build());
    }

    @Transactional(readOnly = true)
    public List<TestEntity> findByTaskId(UUID taskId) {
        return repository.findByTaskId(taskId);
    }
}
