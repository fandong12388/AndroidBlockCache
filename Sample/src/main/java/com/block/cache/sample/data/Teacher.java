package com.block.cache.sample.data;

import com.block.cache.sample.data.Student;

import java.util.List;

/**
 * Created by fandong
 * created on: 2018/10/19
 * description:
 */
public class Teacher {
    public String name;
    public String sex;
    public int age;

    public List<Student> students;

    public Teacher(String name, String sex, int age, List<Student> students) {
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.students = students;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", age=" + age +
                ", students=" + students +
                '}';
    }
}
