package model;

import exception.InvalidInputException;

public class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = requireText(name, "Name");
        if (age < 14 || age > 50) {
            throw new InvalidInputException("Age must be between 14 and 50.");
        }
        this.age = age;
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be blank.");
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    public String getName() { return name; }
    public int getAge() { return age; }

    public String getDisplayInfo() {
        return name + " (age " + age + ")";
    }
}
