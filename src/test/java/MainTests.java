import com.codeforsanjose.blic.Main;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by kylefalconer on 6/11/16.
 */
public class MainTests {
    @Test
    public void printUsageLimits() {
        String usage = Main.getUsage();
        String[] usage_lines = usage.split("\n");
        for (String l : usage_lines){
            assertTrue(l.length() <= 80);
        }

    }
}
