# Secure Child Locator 
## SIRS-2020

In this scenario, consider the problem of child localization in outdoor and indoor spaces.
Develop a solution for smartphone or smartwatch users that enables the tracking of children only by their authorized legal guardians and not by anyone else.
The solution should operate outdoors, using GPS (e.g. A-GPS), but should also operate indoors, where GPS does not work.
Indoor location can rely on Wi-Fi fingerprinting (e.g. Google Indoors Maps), Bluetooth beacons, Li-Fi, or other techniques.

The solution should consider the secure tracking of the children inside defined fences and there should be an alert (SOS) functionality. 
Both the children and the responsible adult should be regarded as users of the system, and all stored and communicated data should consider user consent and their privacy.
If a remote server is used, it should not be fully trusted. 
For example, the solution may assume the server to be _honest-but-curious_, i.e., it follows application protocols but tries to collect as much data as possible about the users.

Developed this project with [Rafael](https://github.com/rafampinto) and [Rita](https://github.com/ritatomasc) during the SIRS course in my master degree at IST.


**References:**

- [My Ki system](https://myki.watch/en/)
- EASYmaxx [Smartwatch](https://www.amazon.de/EASYmaxx-Smartwatch-Armbanduhr-Sprachnachrichten-Standortlokalisierung-blau/dp/B07BZ292D9) and [iOS app](https://apps.apple.com/us/app/easymaxx-smartwatch/id1375209119)
- [Google Maps Indoors](https://www.google.com/maps/about/partners/indoormaps/)

# Certificates configurations

## Generate CA and Server certificates
```bash
  # Generating a pair of keys with OpenSSL
    # Generate the key pair:
    $ openssl genrsa -out ca.key
    
  # Generating a self-signed certificate
    # Create a Certificate Signing Request, using same key:
    $ openssl req -new -key ca.key -out ca.csr
    
  # Self-sign:
    $ openssl x509 -req -days 365 -in ca.csr -signkey ca.key -out ca.crt
 
 # For our certificate to be able to sign other certificates
 # OpenSSL requires that a database exists (a .srl file). Create it:
    $ echo 01 > ca.srl
 
 # Then, generating a key for the server is basically repeating the same steps (see commands above)
 # Except that the self-sign no longer happens and is replaced by:
    $ openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key -out server.crt
 
 # Convert the private key to PKCS8:
    $ openssl pkcs8 -topk8 -inform PEM -outform PEM -in ca.key -nocrypt > ca.pem
    $ openssl pkcs8 -topk8 -inform PEM -outform PEM -in server.key -nocrypt > server\_pkcs8.pem
```

The `ca.pem` file should be placed in both the mobile apps, in the folders `GuardianApp/app/src/main/res/raw` and `ChildApp/app/src/main/res/raw`

The `server\_pkcs8.pem`and `server.crt` files should be placed in the folder `Server/certs`

# Deployment Configurations

After having the virtual machine on, changes must be made to the guardian and child app, this must be done everytime the server ip changes.

Change the server ip in the following files:

  `ChildApp\app\src\main\res\xml\network_security_config.xml`, 
  `ChildApp\app\src\main\java\pt\ulisboa\tecnico\childapp\repository\service\ServerApi.kt`, 
  `GuardianApp\app\src\main\res\xml\network_security_config.xml`,
  `GuardianApp\app\src\main\java\pt\ulisboa\tecnico\guardianapp\repository\service\ServerApi.kt`.


Follow these steps in the virtual machine:

## Install Java 8 
```bash
  $ sudo apt-get update && sudo apt-get upgrade
  $ sudo apt-get install openjdk-8-jdk
  # Check version
  $ java -version 
  # openjdk version "1.8.0_242" ....
```
## Install network utilities
```bash
  $ sudo apt-get update && sudo apt-get upgrade
  $ sudo apt-get install net-tools
```

## Configure Firewall
```bash
  $ sudo apt-get update && sudo apt-get upgrade
  # Install Uncomplicated Firewall
  $ sudo apt install ufw
  $ sudo ufw default deny incoming
  $ sudo ufw default allow outgoing
  # Check status (Should be inactive)
  $ sudo ufw status
  # Allow SSH and SSL/TLS port
  $ sudo ufw allow ssh
  $ sudo ufw allow 8443/tcp
  $ sudo ufw enable
```
To change the configuration of the before.rules file such as mentioned in the report run the command and add or change the desired rules:
```	
sudo nano /etc/ufw/before.rules
```
These rules must be written before the `COMMIT` line in the end of the file.
