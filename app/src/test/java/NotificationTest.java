import android.util.Xml;
import android.widget.Switch;

import org.asteroidos.sync.dataobjects.Notification;
import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;

import java.util.Arrays;

import github.vatsal.easyweather.retrofit.models.Sys;

import static org.junit.Assert.*;

public class NotificationTest {

    Notification notification = new Notification(Notification.MsgType.POSTED,
            "org.test.example",
            434324,
            "Example App",
            "example-app-icon",
            "Very important summary",
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
            "normal");

    @Test
    public void toXML() {
        String shouldXml = "<insert><id>434324</id><pn>org.test.example</pn><vb>normal</vb><an>Example App</an><ai>example-app-icon</ai><su>Very important summary</su><bo>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</bo></insert>";
        String xml = notification.toXML();
        Assert.assertEquals(shouldXml, xml);
    }

    @Test
    public void toBytes() {
        Assert.assertEquals("[60, 105, 110, 115, 101, 114, 116, 62, 60, 105, 100, 62, 52, 51, 52, 51, 50, 52, 60, 47, 105, 100, 62, 60, 112, 110, 62, 111, 114, 103, 46, 116, 101, 115, 116, 46, 101, 120, 97, 109, 112, 108, 101, 60, 47, 112, 110, 62, 60, 118, 98, 62, 110, 111, 114, 109, 97, 108, 60, 47, 118, 98, 62, 60, 97, 110, 62, 69, 120, 97, 109, 112, 108, 101, 32, 65, 112, 112, 60, 47, 97, 110, 62, 60, 97, 105, 62, 101, 120, 97, 109, 112, 108, 101, 45, 97, 112, 112, 45, 105, 99, 111, 110, 60, 47, 97, 105, 62, 60, 115, 117, 62, 86, 101, 114, 121, 32, 105, 109, 112, 111, 114, 116, 97, 110, 116, 32, 115, 117, 109, 109, 97, 114, 121, 60, 47, 115, 117, 62, 60, 98, 111, 62, 76, 111, 114, 101, 109, 32, 105, 112, 115, 117, 109, 32, 100, 111, 108, 111, 114, 32, 115, 105, 116, 32, 97, 109, 101, 116, 44, 32, 99, 111, 110, 115, 101, 116, 101, 116, 117, 114, 32, 115, 97, 100, 105, 112, 115, 99, 105, 110, 103, 32, 101, 108, 105, 116, 114, 44, 32, 115, 101, 100, 32, 100, 105, 97, 109, 32, 110, 111, 110, 117, 109, 121, 32, 101, 105, 114, 109, 111, 100, 32, 116, 101, 109, 112, 111, 114, 32, 105, 110, 118, 105, 100, 117, 110, 116, 32, 117, 116, 32, 108, 97, 98, 111, 114, 101, 32, 101, 116, 32, 100, 111, 108, 111, 114, 101, 32, 109, 97, 103, 110, 97, 32, 97, 108, 105, 113, 117, 121, 97, 109, 32, 101, 114, 97, 116, 44, 32, 115, 101, 100, 32, 100, 105, 97, 109, 32, 118, 111, 108, 117, 112, 116, 117, 97, 46, 32, 65, 116, 32, 118, 101, 114, 111, 32, 101, 111, 115, 32, 101, 116, 32, 97, 99, 99, 117, 115, 97, 109, 32, 101, 116, 32, 106, 117, 115, 116, 111, 32, 100, 117, 111, 32, 100, 111, 108, 111, 114, 101, 115, 32, 101, 116, 32, 101, 97, 32, 114, 101, 98, 117, 109, 46, 32, 83, 116, 101, 116, 32, 99, 108, 105, 116, 97, 32, 107, 97, 115, 100, 32, 103, 117, 98, 101, 114, 103, 114, 101, 110, 44, 32, 110, 111, 32, 115, 101, 97, 32, 116, 97, 107, 105, 109, 97, 116, 97, 32, 115, 97, 110, 99, 116, 117, 115, 32, 101, 115, 116, 32, 76, 111, 114, 101, 109, 32, 105, 112, 115, 117, 109, 32, 100, 111, 108, 111, 114, 32, 115, 105, 116, 32, 97, 109, 101, 116, 46, 60, 47, 98, 111, 62, 60, 47, 105, 110, 115, 101, 114, 116, 62]",
                Arrays.toString(notification.toBytes()));
    }
}