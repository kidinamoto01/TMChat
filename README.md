# TMChat
TMChat is a simple chat-demonstration using the Tendermint-Blockchain as a means to broadcast messages from one user to another.



![alt text](https://github.com/wolfposd/TMChat/raw/master/screenshots/screen1.png "screenshot")




## How to run
This project is handled by [Maven](https://maven.apache.org/) and thus can be build as such. (You will need to have maven installed)

    git clone https://github.com/wolfposd/TMChat.git
    cd TMChat
    mvn package
    
You will then find a runnable JAR in the target-directory, start it as follows:

    java -jar TMChat-0.0.1-SNAPSHOT.jar user1 user2 ... userX
  
  Where user1 - userX are the chatwindows you would like to open. 
  
  The example screenshot was created using:
  
    java -jar TMChat-0.0.1-SNAPSHOT.jar Left Middle Right
    
    
## Introduction

This demo is meant to be a very easy to understand application for tendermint.

![architecture](https://github.com/wolfposd/TMChat/raw/master/screenshots/architecture.png "architecture")

The project is seperated into two parts. We have the NodeCommunication, which connects to the tendermint-node via TMSP to handle all the Tendermint messages like AppendTX, CheckTx and Commit. It also connects via Websocket to the tendermint-node to broadcast TXs it received from its connected clients.

The Frontend is connected to the NodeCommunication. It can send Messages to it (which will be relayed as TXs to the node) and can register for receiving messages (which are converted from TXs). 