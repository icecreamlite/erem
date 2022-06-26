#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ArduinoOTA.h>
#include "time.h"
#include <LiquidCrystal_I2C.h>
#include <Arduino.h>
#include <IRremoteESP8266.h>
#include <IRrecv.h>
#include <IRutils.h>
#include <WebSocketsServer.h>

// WebSocket Globals
WebSocketsServer webSocket = WebSocketsServer(1337);

ESP8266WebServer server(80);
const char* www_username = "WebServer_Username";
const char* www_password = "WebServer_Password";
// allows you to set the realm of authentication Default:"Login Required"
const char* www_realm = "Custom Auth Realm";
// the Content of the HTML response in case of Unautherized Access Default:empty
String authFailResponse = "Authentication Failed";

LiquidCrystal_I2C lcd(0x27, 16, 2);

int time_col = 2;
int screen_num = 0;
bool screen_bool[] = {false, false, false};
bool show_lcd = true;

// Network SSID
const char* ssid = "Network_SSID";
const char* password = "Network_Password";


const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 28800;   //Replace with your GMT offset (seconds)
const int   daylightOffset_sec = 0;  //Replace with your daylight offset (seconds)

const uint16_t kRecvPin = D7;
IRrecv irrecv(kRecvPin);
decode_results results;

// index 0 pin 3 relay 1 - light
// index 1 pin 4 relay 2 - fan
const int rly[] = {D2, D5, D6};
int rly_st[] = {INPUT, INPUT, INPUT};

const char* myEPass = "Extension_Password";

int unlocked = true;
int lock_cc = 0;

unsigned long prev_millis;
unsigned long cur_millis;

bool is_fan_auto = false;
String hhmm;
String faon_time = "07:30";
String faoff_time = "01:00";

char * getLocalTime(const char* time_format, String mmhh24 = "a");

void setup() {
  Serial.begin(115200);
  delay(500);

  lcd.init();
  lcd.backlight();
  lcd.clear();

  // Connect WiFi
  dispLcd("Connecting to", 0, 0, true);
  dispLcd(ssid, 0, 1, false);
  WiFi.mode(WIFI_STA);
  WiFi.hostname("Name");
  WiFi.begin(ssid, password);
  Serial.println("");

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.print("Connected to ");
  Serial.println(ssid);
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  // Start WebSocket server and assign callback
  webSocket.begin();
  webSocket.onEvent(onWebSocketEvent);

  //init and get the time
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  //delay(1000);

  lcd.clear();
  prev_millis = millis();

  irrecv.enableIRIn();  // Start the receiver

  server.on("/et", handleEt);
  server.onNotFound([] {server.send(404);});

  server.begin();
  Serial.println("HTTP server started");






  

  // START OF OTA CODE
  
  // Port defaults to 8266
  // ArduinoOTA.setPort(8266);

  // Hostname defaults to esp8266-[ChipID]
  ArduinoOTA.setHostname("OTA_Hostname");

  // No authentication by default
  ArduinoOTA.setPassword("OTA_Password");

  // Password can be set with it's md5 value as well
  // MD5(admin) = 21232f297a57a5a743894a0e4a801fc3
  // ArduinoOTA.setPasswordHash("21232f297a57a5a743894a0e4a801fc3");

  ArduinoOTA.onStart([]() {
    String type;
    if (ArduinoOTA.getCommand() == U_FLASH) {
      type = "sketch";
    } else { // U_FS
      type = "filesystem";
    }

    // NOTE: if updating FS this would be the place to unmount FS using FS.end()
    Serial.println("Start updating " + type);
  });
  ArduinoOTA.onEnd([]() {
    Serial.println("\nEnd");
  });
  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
    Serial.printf("Progress: %u%%\r", (progress / (total / 100)));
  });
  ArduinoOTA.onError([](ota_error_t error) {
    Serial.printf("Error[%u]: ", error);
    if (error == OTA_AUTH_ERROR) {
      Serial.println("Auth Failed");
    } else if (error == OTA_BEGIN_ERROR) {
      Serial.println("Begin Failed");
    } else if (error == OTA_CONNECT_ERROR) {
      Serial.println("Connect Failed");
    } else if (error == OTA_RECEIVE_ERROR) {
      Serial.println("Receive Failed");
    } else if (error == OTA_END_ERROR) {
      Serial.println("End Failed");
    }
  });
  ArduinoOTA.begin();

  // END OF OTA CODE
}

void loop() {
  ArduinoOTA.handle();
  server.handleClient();
  // Look for and handle WebSocket data
  webSocket.loop();

  cur_millis = millis();

  if (cur_millis - prev_millis >= 1000) {
    if (screen_num == 0) {
      showTime();
    }

    prev_millis = cur_millis;

    if (is_fan_auto) {
      hhmm = getLocalTime("%R");
      if (hhmm == faon_time) {
        rly_func(1, "out");
      }
      if (hhmm == faoff_time) {
        rly_func(1, "in");
      }
    }
  }

  if (irrecv.decode(&results)) {

    switch (results.value) {

      case 0x20DF10EF: // Power, Turn off/on display
        show_lcd = !show_lcd;
        if (show_lcd) {
          lcd.clear();
          lcd.backlight();
          showTime();
          if (!unlocked) {
            dispLcd("LOCK", 12, 1, false);
          }
          screen_num = 0;
        }
        else {
          lcd.clear();
          lcd.noBacklight();
        }
        break;

      case 0x20DF50AF: // DOT, 3x to lock and unlock
        lock_cc++;
        break;

      case 0x20DF8877: // 1, toggle light
        if (unlocked) {
          rly_func(0, "tog");
        }
        break;

      case 0x20DF48B7: // 2, toggle fan
        if (unlocked) {
          rly_func(1, "tog");
        }
        break;

      case 0x20DF1AE5: // Left
        if (unlocked) {
          if (screen_num == 0) {
            screen_num = 3;
          }
          screen_num -= 1;
          screen_bool[screen_num] = true;
        }
        break;

      case 0x20DF9A65: // Right
        if (unlocked) {
          screen_num += 1;
          if (screen_num == 3) {
            screen_num = 0;
          }
          screen_bool[screen_num] = true;
          lcd.clear();
          if (screen_num == 0) {
            showTime();
          }
        }
        break;
    }

    if (lock_cc >= 3) {
      unlocked = !unlocked;
      lcd.clear();
      if (unlocked) {
        time_col = 2;
        showTime();
      }
      else {
        time_col = 0;
        dispLcd("LOCK", 12, 1, false);
        showTime();
        screen_num = 0;
      }

      lock_cc = 0;
    }

    if (screen_bool[0]) {
      lcd.clear();
      showTime();
      screen_bool[0] = false;
    }

    if (screen_bool[1]) {
      showFanAuto();
      screen_bool[1] = false;
    }

    if (screen_bool[2]) {
      dispLcd(ssid, 0, 0, true);
      dispLcd(WiFi.localIP(), 0, 1, false);
      screen_bool[2] = false;
    }

    irrecv.resume();
  }
}

template<typename etirb>
void dispLcd(etirb str_to_disp, int row, int col, bool clr) {
  if (clr) {
    lcd.clear();
  }
  lcd.setCursor(row, col);
  lcd.print(str_to_disp);

}

char * getLocalTime(const char* time_format, String mmhh24) {
  time_t rawtime;
  struct tm * timeinfo;
  time (&rawtime);
  timeinfo = localtime (&rawtime);

  if (mmhh24 != "a") {
    timeinfo->tm_hour = mmhh24.substring(0, 2).toInt();
    timeinfo->tm_min = mmhh24.substring(3).toInt();
  }

  static char str[17];
  strftime(str, sizeof str, time_format, timeinfo);

  return str;
}

void showTime() {
  dispLcd(getLocalTime("%b %d, %Y %a"), 0, 0, false);
  dispLcd(getLocalTime("%r"), time_col, 1, false);
}

void showFanAuto() {
  if (is_fan_auto) {
    dispLcd("FAN OFF", 0, 0, true);
    dispLcd(getLocalTime("%I:%M %p", faoff_time), 8, 0, false);
    dispLcd("FAN ON", 0, 1, false);
    dispLcd(getLocalTime("%I:%M %p", faon_time), 8, 1, false);
  }
  
  else {
    dispLcd("FAN AUTO", 4, 0, true);
    dispLcd("DISABLED", 4, 1, false);
  }
}

void rly_func(int rly_num, String actn) {
  if (actn == "tog") {
    rly_st[rly_num] = !rly_st[rly_num];
  }
  else if (actn == "out") {
    rly_st[rly_num] = OUTPUT;
  }
  else if (actn == "in") {
    rly_st[rly_num] = INPUT;
  }

  pinMode (rly[rly_num], rly_st[rly_num]);
  sendState();
}

void handleEt() {
  if (!server.authenticate(www_username, www_password))
    //Basic Auth Method with Custom realm and Failure Response
    //return server.requestAuthentication(BASIC_AUTH, www_realm, authFailResponse);
    //Digest Auth Method with realm="Login Required" and empty Failure Response
    //return server.requestAuthentication(DIGEST_AUTH);
    //Digest Auth Method with Custom realm and empty Failure Response
    //return server.requestAuthentication(DIGEST_AUTH, www_realm);
    //Digest Auth Method with Custom realm and Failure Response
  {
    return server.requestAuthentication(DIGEST_AUTH, www_realm, authFailResponse);
  }

  if (server.args() == 0) {
    server.send(400);
    return;
  }

  if (!unlocked) {
    sendState();
    server.send(405);
    return;
  }

  String first_arg = server.argName(0);

  if (first_arg == "f") {
    rly_func(1, "tog");
  }

  else if (first_arg == "l") {
    rly_func(0, "tog");
  }

  else if (first_arg == "e") {
    if (server.arg("e") == myEPass) {
      rly_func(2, "tog");
    }
    else {sendState();}
  }

  else if (first_arg == "faon") {
    faon_time = server.arg("faon").substring(0, 2) + ":" + server.arg("faon").substring(3);
    if (screen_num == 1) {showFanAuto();}
    sendFanAuto();
  }

  else if (first_arg == "faof") {
    faoff_time = server.arg("faof").substring(0, 2) + ":" + server.arg("faof").substring(3);
    if (screen_num == 1) {showFanAuto();}
    sendFanAuto();
  }

  else if (first_arg == "fa") {
    if (server.arg("fa") == "enable") {is_fan_auto = true;}
    else if (server.arg("fa") == "disable") {is_fan_auto = false;}
    if (screen_num == 1) {showFanAuto();}
    sendFanAuto();
  }
  
  server.send(200);
}

// Called when receiving any WebSocket message
void onWebSocketEvent(uint8_t num,
                      WStype_t type,
                      uint8_t * payload,
                      size_t length) {

  // Figure out the type of WebSocket event
  switch (type) {

    // Client has disconnected
    case WStype_DISCONNECTED:
      Serial.printf("[%u] Disconnected!\n", num);
      break;

    // New client has connected
    case WStype_CONNECTED:
      {
        IPAddress ip = webSocket.remoteIP(num);
        Serial.printf("[%u] Connection from ", num);
        Serial.println(ip.toString());
        sendState();
      }
      break;

    // Echo text message back to client
    case WStype_TEXT:
    Serial.printf("[%u] Text: %s\n", num, payload);
    if ( strcmp((char *)payload, "queryState") == 0 ) {
      sendState();
    }
    else if ( strcmp((char *)payload, "queryFA") == 0 ) {
      sendFanAuto();
    }
    break;

    // For everything else: do nothing
    // case WStype_TEXT:
    case WStype_BIN:
    case WStype_ERROR:
    case WStype_FRAGMENT_TEXT_START:
    case WStype_FRAGMENT_BIN_START:
    case WStype_FRAGMENT:
    case WStype_FRAGMENT_FIN:
    default:
      break;
  }
}

void sendState() {
  String payload = "state,";
  payload = payload + rly_st[0] + "," + rly_st[1] + "," + rly_st[2];
  Serial.println("Broadcast: " + payload);
  webSocket.broadcastTXT(payload);
}

void sendFanAuto() {
  String payload = "fa,";
  payload = payload + is_fan_auto + "," + getLocalTime("%I:%M %p", faon_time) + "," + getLocalTime("%I:%M %p", faoff_time);
  Serial.println("Broadcast: " + payload);
  webSocket.broadcastTXT(payload);
}
