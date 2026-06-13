package ru.mkenopsia.coderunnerservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mkenopsia.coderunnerservice.model.TestEntity;
import ru.mkenopsia.coderunnerservice.storage.TestJpaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestJpaRepository repository;

    @Transactional
    public TestEntity add(String taskId, String inputData, String expectedOutput) {
        return repository.save(TestEntity.builder()
                .taskId(taskId)
                .inputData(inputData)
                .expectedOutput(expectedOutput)
                .build());
    }

    @Transactional(readOnly = true)
    public List<TestEntity> findByTaskId(String taskId) {
        return repository.findByTaskId(taskId);
    }
}
