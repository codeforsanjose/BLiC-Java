# BLiC-Java
**B**roken **Li**nk **C**hecker written in Java.

The program uses multithreading, retries if it is unable to connect to a webpage or gets a 429 HTTP response code ("too many requests"), and gives a report at the end showing each link tried, along with the result.

To build an executable .jar file, first clone the project, then run this command:

    ./gradlew fatJar
    
   
The jar is saved to `build/libs/blic.jar`, so to run it, do:

    java -jar build/libs/blic.jar [url to test]
