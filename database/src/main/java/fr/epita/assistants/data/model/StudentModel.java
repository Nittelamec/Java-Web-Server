package fr.epita.assistants.data.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.transaction.Transactional;

@Getter
@Setter
@Entity
@Table(name = "student_model")
@Transactional
public class StudentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @ManyToOne(targetEntity = CourseModel.class)
    CourseModel course;

    @Column(name = "name")
    public String name;
}
