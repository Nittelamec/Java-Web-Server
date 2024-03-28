package fr.epita.assistants.data.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "course_model")
@Transactional
public class CourseModel {
    @Column(name = "name")
    public String name;

    @OneToMany(targetEntity = StudentModel.class, mappedBy = "id")
    public List<StudentModel> students;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @ElementCollection
    @CollectionTable(name = "course_model_tags", joinColumns = @JoinColumn(name = "course_id")) List<String> tag;
}
