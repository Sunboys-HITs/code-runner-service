package ru.mkenopsia.coderunnerservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tests")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "test_id", nullable = false, unique = true)
    private UUID testId = UUID.randomUUID();

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "input_data", nullable = false, length = 4096)
    private String inputData;

    @Column(name = "expected_output", nullable = false, length = 4096)
    private String expectedOutput;
}
