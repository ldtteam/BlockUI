### Directory purpose
This directory is setup to be used as a location where a test mod can be stored so that it can be used to test BlockUI.

As opposed to a normal test setup Gradle does not inject unit test runtimes like JUnit in this directory for this project.
It does however inject this sourceset into the Minecraft Forge Runtimes and considers it to be the main directory for running the game.

This infact makes the "main" sourceset behave more like an api then an actuall main, but it made the most sense here.

### Why is there nothing here?
Because I did not have the time to write enough test cases elegantly enough.