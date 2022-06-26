# erem
Control electric extension via Android App, via commandline using Python, or via TV Remote.  
Uses Arduino as webserver to control the relays that switches the extension sockets.  
It also acts as websocket server to sync the state of each extension sockets accross different devices that is using the Android app.

### Via Android App
**Main Layout**  
Toggle switch to turn on and off the electric socket assigned to an appliance.  
<img src="https://github.com/icecreamlite/erem/blob/main/images/main_layout.jpg" alt="main_layout" width="200"/>

Set the IP address to arduino's acquired IP.  
Give the arduino a static IP so you only have to set it once on the app.  
<img src="https://github.com/icecreamlite/erem/blob/main/images/set_ip_layout.jpg" alt="set_ip_layout" width="200"/>

Set the credential needed to authenticate the requests to Arduino webserver.  
<img src="https://github.com/icecreamlite/erem/blob/main/images/set_change_credential.jpg" alt="set_change_credential" width="200"/>

Turning on the Fan Auto will show the auto on/off time of fan and the buttons to adjust those time.
<p float="left">
  <img src="https://github.com/icecreamlite/erem/blob/main/images/without_fan_auto.jpg" alt="without_fan_auto" width="200"/>
  <img src="https://github.com/icecreamlite/erem/blob/main/images/with_fan_auto.jpg" alt="with_fan_auto" width="200"/>
</p>


Changing the fan auto on/off time.  
<img src="https://github.com/icecreamlite/erem/blob/main/images/set_fan_auto.jpg" alt="set_fan_auto" width="200"/>

Use the widget to control the lights and fan.  
<img src="https://github.com/icecreamlite/erem/blob/main/images/widget.jpg" alt="widget" width="200"/>

### Via commadline using python
erem [options]  
  
Options:  
cred {username} {password} - change credentials for webserver  
ip {ip address} - change ip for webserver  
l - toggle light  
f - toggle fan  
fa {enable|disable} - Enable/disable fan auto-on/off  
faon {HH:MM} - set fan auto-on time (24-hr format)  
faof {HH:MM} - set fan auto -off time (24-hr format)  
conf - show erem config file  
  
### Via TV Remote
Currently configured to the remote I'm using.  
  
**Remote buttons**  
Power - Turn on/off the display  
Left/Right - Switches the display to see different information  
1 - Toggle light  
2 - Toggle fan  
Dot - 3x to lock and unlock  
  
Locking the device prevents controlling the extension using the 3 methods
