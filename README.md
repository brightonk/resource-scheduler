# resource-scheduler
Resource Scheduler provides a way to utilize a limit resource(s), by managing the message queuing and prioritisation.

## Overview
System Under Development is a middleware component that receives message from internal components and route the messages to the external resources.
When multiple external resources are used, each resource will have a dedicated connection object that implements the Gateway interface.
Such a connection object is used to communicate with the external resource.

##Score of project
The project will only cover the function that receives the messages and messages dispatching

##Out of Score
Underlying mechanism of receiving the message is out of scope. i.e. messages could be received via a message bus, direct TCP or UDP connection.
Underlying mechanism of dispatching the message is out of scope. i.e. the message could be dispatched to the message queue, message bus, direct TCP or UDP

##Resource Components
The system under development consists of;
 - ResourceScheduler, the class that receives the message. It either immediately dispatches the message or adds the message to a queue if the resource(s) are busy
 - Queue, a data structure for storing the messages awaiting processing. Based on the configured strategy this can be a first-in-first-out queue. It is also possible to configure the queue so that it is based on an arbitrary sorting and group of messages.

