# JakesDiffieHellmanChat
This program implements the Diffie Hellman secure key exchange/agreement.
This allows the client and server to securely exchange messages across any unsafe public network.
After the client and server agree upon a key, they use the first 128 bit of the agreed number to encrypt
all further communication with AES.
