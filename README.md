# quickjs

Example minimal Java Web server+HTML+JS framework that allows server side web application logic to be implemented just like in a component based GUI framework.

Nextgen branch (feature/nextgen) is a redesign based on experience with the first version. It's API is incompatible so some manual work is necessary to upgrade.

The framework has the unique feature that pages are designed as plain HTML and server side objects are attached to them by identifiers.

It is possible to adapt the framework to any Java web server because it has no dependencies to the server implementation.

Jetty based example setup is part of the project.

## Disclaimer

This is not a production ready framework just a technology demo to demonstrate a minimal latency Jetty based web server response.

## Example code

[Example01 code with text editor area online feedback and server push.](https://github.com/rizsi/quickjs/blob/8ce9a8a8b420fff33121184b9d1d310127f6fef6/quickjs-example/src/hu/qgears/quickjs/qpage/example/QExample01.java#L24)

[HTML template of the same page](https://github.com/rizsi/quickjs/blob/8ce9a8a8b420fff33121184b9d1d310127f6fef6/quickjs-example/template/hu/qgears/quickjs/qpage/example/QExample01.java.rt#L58) (implemented using [RTemplate](https://github.com/qgears/rtemplate))

![The example running](example01.png)

An other example fills selection lists with user specified number of objects. This is used to measure the performance of the UI how many objects is handles with acceptable latency:

![Huge selection list](select-from-100000-options.png)


## Executable download and launch

http://rizsi.com/quickjs/quickjs-0.0.0.jar

Usage: java -jar quickjs-0.0.0.jar

The program opens the 127.0.0.1:8888. Launch browser: http://127.0.0.1:8888/

## Details

* Folder can be imported as an Eclipse project
* Runs embedded Jetty server
* Uses rtemplate for efficient and programmer friendly HTML and JS generation. https://github.com/qgears/rtemplate
