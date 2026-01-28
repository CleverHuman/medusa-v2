# 使用多阶段构建
FROM node:20-alpine as builder

ARG ENV=dev
# 设置工作目录
WORKDIR /build

# 复制前端代码
COPY medusa-admin-vue/ /build/

## 当 ENV 为 prod 或 staging 时执行构建
#RUN if [ "$ENV" = "prod" ] || [ "$ENV" = "staging" ] || [ "$ENV" = "dev" ]; then \
#      npm install && npm run build:prod; \
#    else \
#      echo "开发环境：跳过构建"; \
#      mkdir -p dist;\
#    fi

# 当 ENV 为 prod 或 staging 时执行构建
RUN if [ "$ENV" = "prod" ] || [ "$ENV" = "staging" ] || [ "$ENV" = "dev" ]; then \
      # 明确设置npm使用官方registry，避免代理影响
      npm config set registry https://registry.npmjs.org/ && \
      npm config delete proxy && \
      npm config delete https-proxy && \
      # 安装依赖
      npm install && \
      # 构建
      npm run build:prod; \
    else \
      echo "开发环境：跳过构建"; \
      mkdir -p dist;\
    fi


# 最终阶段
FROM nginx:alpine

# 从构建阶段复制产物
COPY --from=builder /build/dist /usr/share/nginx/html/admin

# 复制 Nginx 配置
COPY docker/nginx/nginx.conf /etc/nginx/nginx.conf

# 删除默认配置
RUN rm /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]