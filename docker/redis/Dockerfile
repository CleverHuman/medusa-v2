# 基础镜像
FROM redis:7.0-alpine

# 自定义配置
COPY redis.conf /usr/local/etc/redis/redis.conf

# 暴露端口
EXPOSE 6379

# 启动命令
CMD ["redis-server", "/usr/local/etc/redis/redis.conf"]