set projPath=%~dp0
cd %projPath%
mshta vbscript:createobject("shell.application").shellexecute("%projPath%jre\bin\java","-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=%projPath%temp -jar %projPath%monitor-1.1.0-SNAPSHOT.jar --spring.config.location=%projPath%config\application.yaml --logging.config=%projPath%config\logback.xml","","runas",1)(window.close)