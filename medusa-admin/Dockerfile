FROM amazoncorretto:21

# 设置工作目录
ARG ENV=dev

WORKDIR /app

# 将构建后的 jar 文件复制到 Docker 容器中
COPY ./target/*.jar /app/app.jar
#COPY target/medusa-admin.jar /app/app.jar
# 复制 Flyway 脚本
COPY src/main/resources/db/migration/$ENV /app/db/migration/$ENV


# 暴露端口（如果你的应用需要监听某个端口）
EXPOSE 8080

# 指定容器启动时执行的命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
