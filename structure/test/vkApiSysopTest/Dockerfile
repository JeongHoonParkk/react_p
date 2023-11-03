FROM --platform=linux/amd64 openjdk:17
LABEL maintainer="Cheolmin Jo <cmjo@syszone.kr>"

COPY target/vkApiSysopTest-1.0.0-jar-with-dependencies.jar app.jar

EXPOSE 18097
ENTRYPOINT ["java","--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED","-Xms1250M","-Xmx1250M","-XX:+UseG1GC","-XX:+UseStringDeduplication","-XX:MinHeapFreeRatio=5","-XX:MaxHeapFreeRatio=10","-jar","app.jar"]

# HEALTHCHECK --interval=25s --timeout=5s --retries=2 CMD ["java", "-cp", "app.jar", "kr.syszone.iplt.gw.sgw.rest.HealthCheck", "||", "exit", "1"]
