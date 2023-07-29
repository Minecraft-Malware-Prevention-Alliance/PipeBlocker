package info.mmpa.pipeblocker.test;

import info.mmpa.pipeblocker.ObjectStreamFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectStreamTest {
    @BeforeAll
    public static void applyFilter() {
        ObjectStreamFilter.apply();
    }

    static class DummyRandomObject implements Serializable {
        private String thefield = "string";
    }

    private static void serializeDeserialize(Object o) {
        byte[] data;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(baos);
            os.writeObject(o);
            data = baos.toByteArray();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        try {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data));
            if(stream.readObject() instanceof DummyRandomObject)
                fail("Object should not be deserialized");
        } catch(InvalidClassException ignored) {
            /* expected code path */
        } catch(IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUnsafeObjectBlocked() {
        serializeDeserialize(new DummyRandomObject());
    }

    @Test
    public void testUnsafeObjectInMapBlocked() {
        Map<String, Object> map = new HashMap<>();
        map.put("test", new DummyRandomObject());
        serializeDeserialize(map);
    }
}
