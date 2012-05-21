#include <SPI.h>
#include <Adb.h>

// Adb connection.
Connection * connection;
boolean dosend = false;
int LIGHTPIN = 5;

// Event handler for the shell connection. 
void adbEventHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
   Serial.println("event handler"); 
   if(event !=4 ) {Serial.println(event);}
  // Data packets contain one command byte
  if (event == ADB_CONNECTION_RECEIVE)
  {
     Serial.println("message received"); 

    if(data[0] == 'R') { // Read
        Serial.println("R");
        dosend = true;
    }
    if(data[0] == 'L') { // Read
        if(data[1] == '0') {              
            Serial.println("L0");
            digitalWrite(LIGHTPIN,LOW);
            //analogWrite(5,255);
        }else{
            Serial.println("L1");
            digitalWrite(LIGHTPIN,HIGH);
            //analogWrite(5,0);            
        }          
    }
  }

}


void adbLogCatHandler(Connection * connection, adb_eventType event, uint16_t length, uint8_t * data)
{
  int i;

  if (event == ADB_CONNECTION_RECEIVE)
    for (i=0; i<length; i++){
      String s = (String)data[i];
      Serial.print(s);
    }

}

void setup()
{
  pinMode(LIGHTPIN, OUTPUT);
  pinMode(A0, INPUT);
  digitalWrite(LIGHTPIN,LOW); 
  // Initialise serial port
  Serial.begin(115200);
  
  
  // Initialise the ADB subsystem.  
  ADB::init();
  
   // ADB::addConnection("shell:exec logcat", true, adbLogCatHandler); 

  // Open an ADB stream to the phone's shell. Auto-reconnect
  connection = ADB::addConnection("tcp:7297", true, adbEventHandler);  
  Serial.println("connected");
}

void loop()
{
  

  // Poll the ADB subsystem.
  ADB::poll();
  
  
  if(dosend){
        Serial.println("Sending voltage");
         uint16_t data = analogRead(A0);
        connection->write(2, (uint8_t*)&data);
        dosend =false;
      }

}


