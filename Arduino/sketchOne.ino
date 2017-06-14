#include <CurieBLE.h>

BLEPeripheral peripheral;
BLEService service("FFFF");
BLEIntCharacteristic command("AAAA", BLERead | BLEWrite);

int leftLed = 8;
int rightLed = 10;
int topLed = 12;
int bottomLed = 11;

int nowLed = 13;
int i = 0;

void setup() {
  Serial.begin(9600);

  pinMode(leftLed, OUTPUT);
  pinMode(rightLed, OUTPUT);
  pinMode(topLed, OUTPUT);
  pinMode(bottomLed, OUTPUT);

  peripheral.setLocalName("Awesome101");
  peripheral.setAdvertisedServiceUuid(service.uuid());
  peripheral.addAttribute(service);
  peripheral.addAttribute(command);

  command.setValue(111);

  command.setEventHandler(BLEWritten, commandWrite);

  peripheral.begin();

  Serial.println("Bluetooth device active, waiting for connections...");

}

void loop() {

  BLECentral peripheralCentral = peripheral.central();

  if (peripheralCentral) {
    digitalWrite(13, HIGH);
    Serial.print("Connected to central: ");
    Serial.println(peripheralCentral.address());
    Serial.println(command.uuid());
    Serial.println(service.uuid());
    while (peripheralCentral.connected()) {
      if (command.written()) {
        Serial.println(command.value());
      }
    }
    digitalWrite(13, LOW);
    digitalWrite(leftLed, LOW);
    digitalWrite(rightLed, LOW);
    digitalWrite(topLed, LOW);
    digitalWrite(bottomLed, LOW);
  }
}

void commandWrite(BLECentral& central, BLECharacteristic& characteristic) {

  Serial.println("Characteristic event, written: " + command.value());

  digitalWrite(nowLed, LOW);
  switch (command.value()) {
    case 121 :
      nowLed = leftLed;
      break;
    case 101 :
      nowLed = rightLed;
      break;
    case 211 :
      nowLed = topLed;
      break;
    case 11 :
      nowLed = bottomLed;
      break;
    default :
      nowLed = 13;
  }
  digitalWrite(nowLed, HIGH);

}


