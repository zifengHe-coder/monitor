set projPath=%~dp0
cd %projPath%
%projPath%jre\bin\java -jar %projPath%monitor-1.1.0-SNAPSHOT.jar --spring.config.location=%projPath%config\application.yaml --logging.config=%projPath%config\logback.xml --registerFilePath=%projPath%registerCode.key