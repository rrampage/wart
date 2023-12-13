package rrampage.wasp.lexer;

import java.util.Optional;

public sealed interface Result<T, E> {
    boolean isOk();
    boolean isError();
    Optional <E> err();
    Optional <T> ok();
    String toString();
    record Ok<T, E>(T data) implements Result<T, E>{
        public boolean isOk() {return true;}
        public boolean isError() {return true;}
        public Optional<E> err(){return Optional.empty();}
        public Optional<T> ok(){return Optional.of(data);}
        public String toString() { return String.format("Result Data: %s", data); }
    }
    record Error<T, E>(E error) implements Result<T, E>{
        public boolean isOk() {return false;}
        public boolean isError() {return true;}
        public Optional<E> err(){return Optional.of(error);}
        public Optional<T> ok(){return Optional.empty();}
        public String toString() { return String.format("Result Error: %s", error); }
    }
}
