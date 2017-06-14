#include <CurieBLE.h>

BLEPeripheral blePeripheral;
BLEService bleService("aaaa");
BLEIntCharacteristic bleX("aaa0", BLERead | BLEWrite);
BLEIntCharacteristic bleY("aaa1", BLERead | BLEWrite);

int leftLed = 9;
int rightLed = 10;
int bottomLed = 11;
int topLed = 12;

int nowLed = 13;

bool hasCommand = false;
bool hasCommandC[] = {false, false};

int delayTime = 1000;
String dir = "";

void setup() {
  Serial.begin(9600);

  for (int i = 9; i < 14; i++) {
    pinMode(i, OUTPUT);
  }

  blePeripheral.setLocalName("Awesome101");
  blePeripheral.setDeviceName("Awesome101");

  blePeripheral.setAdvertisedServiceUuid(bleService.uuid());

  blePeripheral.addAttribute(bleService);
  blePeripheral.addAttribute(bleX);
  blePeripheral.addAttribute(bleY);

  blePeripheral.setEventHandler(BLEConnected, blePeripheralConnect);
  blePeripheral.setEventHandler(BLEDisconnected, blePeripheralDisonnect);

  bleX.setValue(0);
  bleX.setEventHandler(BLEWritten, bleXWritten);

  bleY.setValue(0);
  bleY.setEventHandler(BLEWritten, bleYWritten);

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

void bleXWritten(BLECentral& central, BLECharacteristic& characteristic) {
  executeCommand(bleX.value(),leftLed, rightLed, 0);
}

void bleYWritten(BLECentral& central, BLECharacteristic& characteristic) {
  executeCommand(bleY.value(),topLed, bottomLed, 1);
}

void executeCommand(int command, int led1, int led2, int i) {
  hasCommandC[i] = true;

  digitalWrite(led1, LOW);
  digitalWrite(led2, LOW);

  if (command == 1) {
    digitalWrite(led1, HIGH);
  } else if (command == 2) {
    digitalWrite(led2, HIGH);
  } else {
    hasCommandC[i] = false;
  }

  if (hasCommandC[0] || hasCommandC[1]) {
    hasCommand = true;
  } else {
    hasCommand = false;
  }
}
