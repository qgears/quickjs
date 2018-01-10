# quickjs

Example minimal Java Web server+HTML+JS framework that allows server side web application logic to be implemented just like in a component based GUI framework.

The framework has the unique feature that pages are designed as plain HTML and server side objects are attached to them by identifiers.

It is possible to adapt the framework to any Java web server because it has no dependencies to the server implementation.

Jetty based example setup is part of the project.

## Disclaimer

This is not a production ready framework just a technology demo to demonstrate a minimal latency Jetty based web server response.

## Executable download and launch

http://rizsi.com/quickjs/quickjs-0.0.0.jar

Usage: java -jar quickjs-0.0.0.jar

The program opens the 127.0.0.1:8888. Launch browser: http://127.0.0.1:8888/

## Details

* Folder can be imported as an Eclipse project
* Runs embedded Jetty server
* Uses rtemplate for efficient and programmer friendly HTML and JS generation. https://github.com/qgears/rtemplate
