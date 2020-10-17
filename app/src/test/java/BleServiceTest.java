import org.asteroidos.sync.dataobjects.BleCharacteristic;
import org.asteroidos.sync.dataobjects.BleService;
import org.asteroidos.sync.utils.AsteroidUUIDS;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class BleServiceTest {

    BleCharacteristic testChar0 = new BleCharacteristic("testChar_char",
            AsteroidUUIDS.MEDIA_ARTIST_CHAR,
            false);
    BleCharacteristic testChar1 = new BleCharacteristic("anotherTest_char",
            UUID.randomUUID(),
            true);

    BleCharacteristic testChar2 = new BleCharacteristic("third_char",
            AsteroidUUIDS.NOTIFICATION_FEEDBACK_CHAR,
            true);


    @Test
    public void createAndReadService() {

        BleCharacteristic[] localTestServiceChars = new BleCharacteristic[2];
        localTestServiceChars[0] = testChar0;
        localTestServiceChars[1] = testChar1;

        Assert.assertNotNull(localTestServiceChars);
        Assert.assertNotNull(localTestServiceChars[0]);
        Assert.assertNotNull(localTestServiceChars[1]);

        BleService localTestService = new BleService("localTest_service",
                AsteroidUUIDS.BATTERY_SERVICE_UUID,localTestServiceChars);

        Assert.assertEquals("localTest_service", localTestService.getServiceName());
        Assert.assertEquals(AsteroidUUIDS.BATTERY_SERVICE_UUID, localTestService.getServiceUUID());

        assert localTestService.getServiceCharacteristics() != null;

        int i = 0;
        for (BleCharacteristic c: localTestService.getServiceCharacteristics()) {
            c.printCharacteristicInfo();
            Assert.assertEquals(localTestServiceChars[i], localTestService.getServiceCharacteristics()[i]);

            Assert.assertEquals(localTestServiceChars[i].isReadableCharactersitic(), localTestService.getServiceCharacteristics()[i].isReadableCharactersitic());
            Assert.assertEquals(localTestServiceChars[i].getCharacteristicName(), localTestService.getServiceCharacteristics()[i].getCharacteristicName());
            Assert.assertEquals(localTestServiceChars[i].getCharacteristicUUID(), localTestService.getServiceCharacteristics()[i].getCharacteristicUUID());

            i++;
        }
    }

    @Test
    public void addCharacteristicAfterServiceCreation() {
        BleCharacteristic[] localTestServiceChars = new BleCharacteristic[2];
        localTestServiceChars[0] = testChar0;
        localTestServiceChars[1] = testChar1;

        Assert.assertNotNull(localTestServiceChars);
        Assert.assertNotNull(localTestServiceChars[0]);
        Assert.assertNotNull(localTestServiceChars[1]);

        BleService localTestService = new BleService("localTest_service",
                AsteroidUUIDS.BATTERY_SERVICE_UUID);

        Assert.assertEquals("localTest_service", localTestService.getServiceName());
        Assert.assertEquals(AsteroidUUIDS.BATTERY_SERVICE_UUID, localTestService.getServiceUUID());

        localTestService.addCharacteristics(localTestServiceChars);

        int i = 0;
        for (BleCharacteristic c: localTestService.getServiceCharacteristics()) {
            c.printCharacteristicInfo();
            Assert.assertEquals(localTestServiceChars[i], localTestService.getServiceCharacteristics()[i]);

            Assert.assertEquals(localTestServiceChars[i].isReadableCharactersitic(), localTestService.getServiceCharacteristics()[i].isReadableCharactersitic());
            Assert.assertEquals(localTestServiceChars[i].getCharacteristicName(), localTestService.getServiceCharacteristics()[i].getCharacteristicName());
            Assert.assertEquals(localTestServiceChars[i].getCharacteristicUUID(), localTestService.getServiceCharacteristics()[i].getCharacteristicUUID());

            i++;
        }
    }

    @Test
    public void addMoreCharacteristicAfterServiceCreation() {
        BleCharacteristic[] localTestServiceChars = new BleCharacteristic[2];
        localTestServiceChars[0] = testChar0;
        localTestServiceChars[1] = testChar1;

        Assert.assertNotNull(localTestServiceChars);
        Assert.assertNotNull(localTestServiceChars[0]);
        Assert.assertNotNull(localTestServiceChars[1]);

        BleService localTestService = new BleService("localTest_service",
                AsteroidUUIDS.BATTERY_SERVICE_UUID, localTestServiceChars);

        Assert.assertEquals("localTest_service", localTestService.getServiceName());
        Assert.assertEquals(AsteroidUUIDS.BATTERY_SERVICE_UUID, localTestService.getServiceUUID());

        localTestService.addCharacteristic(testChar2);

        Assert.assertEquals(localTestServiceChars.length+1, localTestService.getServiceCharacteristics().length);

        int i = 0;
        assert localTestService.getServiceCharacteristics() != null;
        for (BleCharacteristic c: localTestService.getServiceCharacteristics()) {
            System.out.println("\nAt index position " + i + ":");
            c.printCharacteristicInfo();
            i++;
        }

        Assert.assertEquals(testChar0, localTestService.getServiceCharacteristics()[0]);
        Assert.assertEquals(testChar1, localTestService.getServiceCharacteristics()[1]);
        Assert.assertEquals(testChar2, localTestService.getServiceCharacteristics()[2]);
    }

}