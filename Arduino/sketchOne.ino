#include <CurieBLE.h>

BLEPeripheral ble;
BLEService service("00000000-0000-0000-0000-000000000010");
BLEIntCharacteristic command("00000000-0000-0000-0000-000000000001", BLERead | BLEWrite);

void setup() {
  Serial.start(9600);
  ble.setLocalName("Awesome101");
  ble.setDeviceName("Awesome101");
  ble.setAdvertisedServiceUuid(service.uuid());
  ble.setEventHandler(BLEConnected, connect);
  ble.setEventHandler(BLEDisconnected, disonnect);
  ble.addAttribute(service);
  ble.addAttribute(command);
  command.setValue(0);
  command.setEventHandler(BLEWritten, commandReceived);
  ble.begin();
  pinMode(13, OUTPUT);
}

void connect(BLECentral& central) { digitalWrite(13, HIGH); }
void disonnect(BLECentral& central) { digitalWrite(13, LOW); }

void commandReceived(BLECentral& central, BLECharacteristic& characteristic) {
  int c = command.value();
  Serial.println(c);
}

void loop() {}
