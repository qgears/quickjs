# quickjs

Example minimal embedded Jetty+JS client.

The values of the input fields are sent to the server on each change. The bottom part is refreshed through the server.

## Disclaimer

This is not a real program just a technology demo to demonstrate a minimal latency Jetty based web server response.

Input fields are not validated and escaped.

## Executable download and launch

http://rizsi.com/quickjs/quickjs.jar

Usage: java -jar quickjs.jar

The program opens the 127.0.0.1:8888. Launch browser: http://127.0.0.1:8888/

## Details

* Folder can be imported as an Eclipse project
* Runs embedded Jetty server
* Uses rtemplate for efficient and programmer friendly HTML and JS generation. https://github.com/qgears/rtemplate
