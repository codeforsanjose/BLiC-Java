# BLiC-Java
**B**roken **Li**nk **C**hecker written in Java.

The program uses multithreading, retries if it is unable to connect to a webpage or gets a 429 HTTP response code ("too many requests"), and gives a report at the end showing each link tried, along with the result.

If you have [gradle installed](https://docs.gradle.org/current/userguide/installation.html) (on a Mac with homebrew, this is just `brew update && brew install gradle`), you can build an executable .jar file by first cloning the project, then running this command inside the project root:

    gradle fatJar
    
   
The jar is saved to `build/libs/blic.jar`, so to run it, do:

    java -jar build/libs/blic.jar [url to test]
