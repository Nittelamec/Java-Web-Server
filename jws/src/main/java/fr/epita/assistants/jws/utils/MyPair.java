package fr.epita.assistants.jws.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPair<A, B> {
    A a;
    B b;

    public MyPair(A a, B b) {
        this.a = a;
        this.b = b;
    }
}
