import info.mmpa.pipeblocker.ObjectStreamFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectStreamTest {
    @BeforeAll
    public static void applyFilter() {
        ObjectStreamFilter.apply();
    }

    @Test
    public void testUnsafeObjectBlocked() {
        byte[] data;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(baos);
            os.writeObject("my test string");
            data = baos.toByteArray();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        try {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(data));
            stream.readObject();
        } catch(IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
