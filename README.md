_Please note this project is not maintained by anyone and if you would like to participate contact us at <dev@std42.ru>_

elFinder servlet (Java/J2EE)
============================

Description
-----------

This is a Java connector (backend) for elFinder.

Important! This project is a generic library which can't be run directly.
All you need is to integrate it in your own project:
- as a maven dependency (org.elfinder:elfinder-servlet)
- or as an external Jar (jar is available in /jar directory)

Have a look to the "elfinder-servlet-demo" project to see a runnable example:
https://github.com/Studio-42/elfinder-servlet-demo


Features
--------

 * Basic operations (copy, move, upload, create folder/file, rename, etc.)
 * UTF-8
 * Multiple configurations, useful for setting up multiple user areas
 * Thumbnails

Requirements
------------

Server:
 * J2EE
 * Few libraries described in pom.xml (servlet-api, JSON, fileupload, mime-util)


Documentation
-------------

...doc is in da code, sorry :(

=> NEW! Have a look to "elfinder-servlet-demo" project to see a runnable project.

In a few words and as quick start guide, you should:

1. extend `AbstractConnectorConfig` to implement your own backend configuration (essentially to provide root directory, root URL and filesystem implementation - you may use `DiskFsImpl`) and eventually override default behaviors (like max upload size, date format...)
	See `my.demo.MyDemoConfig` for an example from the demo project
    
2. extend AbstractConnectorServlet and implement prepareConfig(), for returning your own backend configuration
	See `my.demo.MyDemoServlet` for an example from the demo project
    
3. then use your own servlet like any Java servlet (put it to your web.xml)
 
You may also:

* override default commands behaviors:

	`org.elfinder.servlets.commands.MkdirCommandOverride` overrides `org.elfinder.servlets.commands.MkdirCommand`
	`org.elfinder.servlets.commands.AbstractCommandOverride` overrides `org.elfinder.servlets.commands.AbstractCommand` (this is superclass of all commands)

* implement new commands by creating a new class like `org.elfinder.servlets.commands.YourOwnCommand` extending `AbstractCommandOverride`


Authors
-------

Initial developer: Antoine Walter _anw!nospam!@anw.fr_
Thumbnails & elfinder demo: özkan pakdil

License
-------

This software is available under a 3-clauses BSD license:

<pre>
Copyright (c) 2009-2011, Studio 42 Ltd.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Studio 42 Ltd. nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Studio 42 Ltd. ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Studio 42 Ltd. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
</pre>
