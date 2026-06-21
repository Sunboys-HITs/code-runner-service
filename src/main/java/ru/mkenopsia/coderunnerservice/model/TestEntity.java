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
    @Column(name = "\"Id\"")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "input_data", nullable = false, length = 4096)
    private String inputData;

    @Column(name = "expected_output", nullable = false, length = 4096)
    private String expectedOutput;
}
