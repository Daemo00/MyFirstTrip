package com.daemo.myfirsttrip.models;


public class Person {
    public final int id;
    public String name;
    public String surname;
    public boolean isDraft = false;
    public int old_id = -1;

    public Person(int id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
