#include <CurieBLE.h>

BLEPeripheral blePeripheral;
BLEService bleService("aaaa");
BLEIntCharacteristic bleX("aaa1", BLERead | BLEWrite);
BLEIntCharacteristic bleY("aaa0", BLERead | BLEWrite);

bool hasCommand = false;

int delayTime = 1000;

int x = 0;
int y = 0;

int controls[2][3] = {{3, 4, 6}, {7, 8, 5}};

void setup() {

  for (int i = 3; i < 9; i++) {
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

  pinMode(13, OUTPUT);
  digitalWrite(13, HIGH);
  delay(1000);
  digitalWrite(13, LOW);
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
  digitalWrite(13, HIGH);
  delayTime = 100;
}

void blePeripheralDisonnect(BLECentral& central) {
  hasCommand = false;
  delayTime = 1000;
}

void bleXWritten(BLECentral& central, BLECharacteristic& characteristic) {
  x = bleX.value();

  changeDirection(0);

}

void bleYWritten(BLECentral& central, BLECharacteristic& characteristic) {
  y = bleY.value();
  changeDirection(1);
}

//engine number: 0 - left; 1 - right
//dir: 0 - stop; 1 - forward; 2 - backward
//power: 0 - off; 255 - on;

void launchEngine(int number, int dir, int power) {

  if (!dir) power = 0;

  if (power > 255) power = 255;
  else if (power < 0) power = 0;

  setDirection(number, dir);
  analogWrite(controls[number][2], power);

}

void stopEngine(int number) {
  analogWrite(controls[number][2], 0);
}

void setDirection(int number, int dir) {
  if (dir) {
    bool b = dir == 1;
    digitalWrite(controls[number][0], !b);
    digitalWrite(controls[number][1], b);
  }
}

void changeDirection(int i) {

  int dir0 = x;
  int dir1 = dir0;

  if (!x) {
    if (!y) {
      stopEngine(0);
      stopEngine(1);
      return;
    } else {
      dir1 = y;
      dir0 = y == 1 ? 2 : 1;
    }
  }

  launchEngine(0, dir0, 255);
  launchEngine(1, dir1, 255);

  if (x && y) {
    stopEngine(y - 1);
  }

  if (x || y) {
    hasCommand = true;
  } else {
    hasCommand = false;
  }

}
