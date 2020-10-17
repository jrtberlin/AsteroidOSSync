import org.asteroidos.sync.dataobjects.BleCharacteristic;
import org.asteroidos.sync.utils.AsteroidUUIDS;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class BleCharacteristicTest {

    BleCharacteristic testChar0 = new BleCharacteristic("testChar_char",
            AsteroidUUIDS.MEDIA_ARTIST_CHAR,
            false);
    BleCharacteristic testChar1 = new BleCharacteristic("anotherTest_char",
            UUID.randomUUID(),
            true);


    @Test
    public void createAndReadCharacteristic() {
        BleCharacteristic localTestChar = new BleCharacteristic("loremipsum_char", AsteroidUUIDS.BATTERY_UUID, true);

        Assert.assertFalse(localTestChar.isReadableCharactersitic());
        Assert.assertTrue(localTestChar.isWriteableCharacteristic());

        Assert.assertEquals("loremipsum_char", localTestChar.getCharacteristicName());
        Assert.assertEquals(AsteroidUUIDS.BATTERY_UUID, localTestChar.getCharacteristicUUID());

        Assert.assertEquals(localTestChar, localTestChar);

        Assert.assertTrue(testChar0.isReadableCharactersitic());
        Assert.assertFalse(testChar0.isWriteableCharacteristic());

        Assert.assertEquals("testChar_char", testChar0.getCharacteristicName());
        Assert.assertEquals(AsteroidUUIDS.MEDIA_ARTIST_CHAR, testChar0.getCharacteristicUUID());

        Assert.assertEquals(testChar0, testChar0);
    }

}