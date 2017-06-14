#include <CurieBLE.h>

BLEPeripheral blePeripheral;
BLEService bleService("FFFF");
BLEIntCharacteristic bleIntChar("AAAA", BLERead | BLEWrite);

int leftLed = 9;
int rightLed = 10;
int bottomLed = 11;
int topLed = 12;
int nowLed = 13;

bool hasCommand = false;
int delayTime = 1000;

void setup() {
  Serial.begin(9600);

  for (int i = 9; i < 14; i++) {
    pinMode(i, OUTPUT);
  }

  blePeripheral.setLocalName("Awesome101");
  blePeripheral.setDeviceName("Awesome101");

  blePeripheral.setAdvertisedServiceUuid(bleService.uuid());

  blePeripheral.addAttribute(bleService);
  blePeripheral.addAttribute(bleIntChar);

  blePeripheral.setEventHandler(BLEConnected, blePeripheralConnect);
  blePeripheral.setEventHandler(BLEDisconnected, blePeripheralDisonnect);

  bleIntChar.setValue(111);
  bleIntChar.setEventHandler(BLEWritten, bleIntCharWritten);

  blePeripheral.begin();

  for (int i = 9; i < 13; i++) {
    digitalWrite(i, HIGH);
    delay(100);
  }

  delay(500);

  for (int i = 12; i > 8; i--) {
    digitalWrite(i, LOW);
    delay(100);
  }

}

void loop() {
  if (!hasCommand) {
    digitalWrite(13, HIGH);
    delay(100);
    digitalWrite(13, LOW);
    delay(delayTime);
  }
}

void blePeripheralConnect(BLECentral& central) {
  Serial.print("Connected to central: ");
  Serial.println(central.address());
  digitalWrite(13, HIGH);
  delayTime = 100;
}

void blePeripheralDisonnect(BLECentral& central) {
  Serial.print("Disconnected from central: ");
  Serial.println(central.address());
  for (int i = 9; i < 14; i++) {
    digitalWrite(i, LOW);
  }

  hasCommand = false;
  delayTime = 1000;
}

void bleIntCharWritten(BLECentral& central, BLECharacteristic& characteristic) {

  String dir = "";

  hasCommand = true;

  digitalWrite(nowLed, LOW);
  switch (bleIntChar.value()) {
    case 121 :
      nowLed = leftLed;
      dir = "left";
      break;
    case 101 :
      nowLed = rightLed;
      dir = "right";
      break;
    case 211 :
      nowLed = topLed;
      dir = "top";
      break;
    case 11 :
      nowLed = bottomLed;
      dir = "bottom";
      break;
    default :
      nowLed = 13;
      dir = "nothing";
      hasCommand = false;
  }
  digitalWrite(nowLed, HIGH);

  Serial.println("Command recieved: " + dir);
}
