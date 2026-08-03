package com.microservice.course.service;

import com.microservice.course.entity.Course;
import com.microservice.course.http.response.StudentByCourseResponse;

import java.util.List;

public interface ICourseService {

    List<Course> findAll();

    Course findById(Long id);

    void save(Course course);


    //respuesta personalizada para el micorservicio curso
    StudentByCourseResponse findStudentByIdCourse(Long idCourse);
}
