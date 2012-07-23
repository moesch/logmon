package cfg;

import junit.framework.Assert;

import org.junit.Test;

public class TestUtils {

    @Test
    public void testResolvENV() {

        String in = "/pre/$ENV{USERNAME}/post";

        String out = Util.resolvENV(in);

        System.out.println("Resolved string is: "+out);
        String usern=System.getenv("USERNAME");

        String ok="/pre/"+usern+"/post";

        Assert.assertTrue(out.equals(ok));

    }

}
