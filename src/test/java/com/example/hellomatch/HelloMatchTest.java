package com.example.hellomatch;

import static com.example.hellomatch.HelloMatch.main;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class HelloMatchTest {
    @Test
    void myTest() {
        try {
            HelloMatch.main(new String[]{"one"});
            System.out.println("We succeeded");
        } catch (Exception  e) {
            System.out.println(e);
            System.out.println("We had an error");
        }
    }
}