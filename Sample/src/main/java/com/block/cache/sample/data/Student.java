package com.block.cache.sample.data;

import java.io.Serializable;

/**
 * Created by fandong
 * created on: 2018/10/19
 * description:
 */
public class Student{

    public String name;
    public String sex;
    public int age;

    public Student(String name, String sex, int age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", age=" + age +
                '}';
    }
}
