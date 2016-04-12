package ut.com.nxp.bj.tinker;

import org.junit.Test;
import com.nxp.bj.tinker.MyPluginComponent;
import com.nxp.bj.tinker.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}